using emailing_service.Utils.Exception;
using MySqlConnector;

namespace emailing_service.Models.Database;

public class DatabaseHelper : IDatabaseHelper
{
    public static string _connectionString ;

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

                var command = new MySqlCommand("CREATE TABLE IF NOT EXISTS emails (id INT AUTO_INCREMENT PRIMARY KEY, email VARCHAR(255), subject VARCHAR(255), body TEXT, emailStatus VARCHAR(50))", connection);
                await command.ExecuteNonQueryAsync();
                success = true; // Exit loop if the command succeeds

                Console.WriteLine("Table creation successful.");
            }
            catch (MySqlException ex)
            {
                currentRetry++;
                Console.WriteLine($"Attempt {currentRetry}: Failed to create table. Retrying in 2 seconds... Error: {ex.Message}");
                await Task.Delay(2000); // Wait for 2 seconds before retrying
            }
        }
        if (!success)
        {
            Console.WriteLine("Failed to create table after multiple attempts.");
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
        catch (MySqlException ex)
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
        catch (MySqlException e)
        {
            throw new MissingDatabaseException("Could not find the database");
        }
        
    }
}