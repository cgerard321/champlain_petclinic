using emailing_service.Utils.Exception;
using MySqlConnector;

namespace emailing_service.Models.Database;

public class DatabaseHelper : IDatabaseHelper
{
    public static string _connectionString ;

    public DatabaseHelper()
    {
        
    }

    public DatabaseHelper(string connectionString)
    {
        _connectionString = connectionString;
    }


    public async Task CreateTableAsync(int ticks)
    {
        int maxRetries = ticks; // This allows retrying for 10 seconds (50 * 0.2s)
        int currentRetry = 0;
        bool success = false;

        while (currentRetry < maxRetries && !success)
        {
            try
            {
                using var connection = new MySqlConnection(_connectionString);
                await connection.OpenAsync();

                // Create the first table
                var command = new MySqlCommand("CREATE TABLE IF NOT EXISTS emails (id INT AUTO_INCREMENT PRIMARY KEY, email VARCHAR(255), subject VARCHAR(255), body TEXT, emailStatus VARCHAR(50))", connection);
                await command.ExecuteNonQueryAsync();
                Console.WriteLine("Table 'emails' creation successful.");

                // Create the second table for EmailModelNotification
                var notificationCommand = new MySqlCommand("CREATE TABLE IF NOT EXISTS email_notifications (id INT AUTO_INCREMENT PRIMARY KEY, email VARCHAR(255), subject VARCHAR(255), body TEXT, emailStatus VARCHAR(50), sendTime DATETIME)", connection);
                await notificationCommand.ExecuteNonQueryAsync();
                Console.WriteLine("Table 'email_notifications' creation successful.");

                success = true; // Exit loop if the command succeeds
            }
            catch (MySqlException ex)
            {
                currentRetry++;
                Console.WriteLine($"Attempt {currentRetry}: Failed to create tables. Retrying in 2 seconds... Error: {ex.Message}");
                await Task.Delay(2000); // Wait for 2 seconds before retrying
            }
        }

        if (!success)
        {
            Console.WriteLine("Failed to create tables after multiple attempts.");
            throw new MissingDatabaseException("Could not find or create the database after multiple attempts.");
        }
    }



    public async Task AddEmailAsync(string email, string subject, string body, string emailStatus)
    {
        try
        {
            using var connection = new MySqlConnection(_connectionString);
            await connection.OpenAsync();
            
                var command =
                    new MySqlCommand(
                        "INSERT INTO emails (email, subject, body, emailStatus) VALUES (@email, @subject, @body, @emailStatus)",
                        connection);
                command.Parameters.AddWithValue("@email", email);
                command.Parameters.AddWithValue("@subject", subject);
                command.Parameters.AddWithValue("@body", body);
                command.Parameters.AddWithValue("@emailStatus", emailStatus);
                await command.ExecuteNonQueryAsync();
        }
        catch (MySqlException)
        {
            throw new MissingDatabaseException("Could not find or create the database.");
        }
        
    }
    
    public async Task AddEmailNotificationAsync(string email, string subject, string body, string emailStatus, DateTime sendTime)
    {
        try
        {
            using var connection = new MySqlConnection(_connectionString);
            await connection.OpenAsync();
        
            var command =
                new MySqlCommand(
                    "INSERT INTO email_notifications (email, subject, body, emailStatus, sendTime) VALUES (@email, @subject, @body, @emailStatus, @sendTime)",
                    connection);
            command.Parameters.AddWithValue("@email", email);
            command.Parameters.AddWithValue("@subject", subject);
            command.Parameters.AddWithValue("@body", body);
            command.Parameters.AddWithValue("@emailStatus", emailStatus);
            command.Parameters.AddWithValue("@sendTime", sendTime);
            await command.ExecuteNonQueryAsync();
        }
        catch (MySqlException)
        {
            throw new MissingDatabaseException("Could not find or create the database.");
        }
    }
    
    public async Task<List<EmailModel>> GetAllEmailsAsync()
    {
        try
        {
            using var connection = new MySqlConnection(_connectionString);
            await connection.OpenAsync();

            var command = new MySqlCommand("SELECT * FROM emails", connection);
            var reader = await command.ExecuteReaderAsync();

            var emails = new List<EmailModel>();

            while (await reader.ReadAsync())
            {
                emails.Add(new EmailModel
                {
                    Id = reader.GetInt32(0),
                    Email = reader.GetString(1),
                    Subject = reader.GetString(2),
                    Body = reader.GetString(3),
                    EmailStatus = reader.GetString(4)
                });
            }
            return emails;
        }
        catch (MySqlException)
        {
            throw new MissingDatabaseException("Could not find the database");
        }
    }
    public async  Task<List<EmailModelNotification>> GetAllEmailsNotificationAsync()
    {
        try
        {
            using var connection = new MySqlConnection(_connectionString);
            await connection.OpenAsync();

            var command = new MySqlCommand("SELECT * FROM email_notifications", connection);
            var reader = await command.ExecuteReaderAsync();

            var emails = new List<EmailModelNotification>();

            while (await reader.ReadAsync())
            {
                emails.Add(new EmailModelNotification()
                {
                    Id = reader.GetInt32(0),
                    Email = reader.GetString(1),
                    Subject = reader.GetString(2),
                    Body = reader.GetString(3),
                    EmailStatus = reader.GetString(4),
                    SendTime = reader.GetDateTime(5)
                });
            }
            return emails;
        }
        catch (MySqlException)
        {
            throw new MissingDatabaseException("Could not find the database");
        }
    }



    public async Task MoveEmailNotificationToEmailsAsync(int id, String result)
    {
        try
        {
            using var connection = new MySqlConnection(_connectionString);
            await connection.OpenAsync();

            // Begin a transaction to ensure atomicity
            using var transaction = await connection.BeginTransactionAsync();

            try
            {
                // Step 1: Retrieve the email notification by ID
                var selectCommand = new MySqlCommand(
                    "SELECT email, subject, body, emailStatus FROM email_notifications WHERE id = @id",
                    connection, transaction);
                selectCommand.Parameters.AddWithValue("@id", id);

                EmailModelNotification? notification = null;

                using (var reader = await selectCommand.ExecuteReaderAsync())
                {
                    if (await reader.ReadAsync())
                    {
                        // Step 2: Extract data from the email notification
                        notification = new EmailModelNotification
                        {
                            Email = reader.GetString(0),
                            Subject = reader.GetString(1),
                            Body = reader.GetString(2),
                            EmailStatus = result
                        };
                    }
                    else
                    {
                        throw new KeyNotFoundException("Email notification with the specified ID was not found.");
                    }
                } // Close the reader here to release the connection

                if (notification != null)
                {
                    // Step 3: Insert the data into the emails table
                    var insertCommand = new MySqlCommand(
                        "INSERT INTO emails (email, subject, body, emailStatus) VALUES (@email, @subject, @body, @emailStatus)",
                        connection, transaction);
                    insertCommand.Parameters.AddWithValue("@email", notification.Email);
                    insertCommand.Parameters.AddWithValue("@subject", notification.Subject);
                    insertCommand.Parameters.AddWithValue("@body", notification.Body);
                    insertCommand.Parameters.AddWithValue("@emailStatus", notification.EmailStatus);

                    await insertCommand.ExecuteNonQueryAsync();

                    // Step 4: Delete the email notification from the email_notifications table
                    var deleteCommand = new MySqlCommand(
                        "DELETE FROM email_notifications WHERE id = @id",
                        connection, transaction);
                    deleteCommand.Parameters.AddWithValue("@id", id);

                    await deleteCommand.ExecuteNonQueryAsync();
                }

                // Commit the transaction
                await transaction.CommitAsync();
            }
            catch (Exception)
            {
                // Rollback the transaction in case of an error
                await transaction.RollbackAsync();
                throw; // Re-throw the exception after rollback
            }
        }
        catch (MySqlException ex)
        {
            throw new MissingDatabaseException("Could not find the database. Error: " + ex.Message);
        }
    }
}