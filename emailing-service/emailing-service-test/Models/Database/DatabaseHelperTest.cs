using emailing_service.Models.Database;
using emailing_service.Utils.Exception;
using Moq;
using MySqlConnector;

namespace emailing_service_test.Models.Database;
[TestFixture]
public class DatabaseHelperTest
{


    [SetUp]
    public void Setup()
    {
        // Initialize the mocks

    }
    [Test]
    public Task CreateTableAsync_ThrowsException_WhenDatabaseDoesNotExist()
    {
        // Arrange
        IDatabaseHelper databaseHelperTest = new DatabaseHelper();
        // Act and Assert
        Assert.ThrowsAsync<MissingDatabaseException>(() =>databaseHelperTest.CreateTableAsync(1));
        return Task.CompletedTask;
    }
    [Test]
    public Task AddEmailAsync_ThrowsException_WhenDatabaseDoesNotExist()
    {
        // Arrange
        IDatabaseHelper databaseHelperTest = new DatabaseHelper();
        // Act and Assert
        Assert.ThrowsAsync<MissingDatabaseException>(() =>databaseHelperTest.AddEmailAsync(
                "test@test.com",
                "exampleSubject",
                "body",
                "status"
            ));
        return Task.CompletedTask;
    }
    [Test]
    public Task AddEmailNotificationAsync()
    {
        IDatabaseHelper databaseHelperTest = new DatabaseHelper();
        // Act and Assert
        Assert.ThrowsAsync<MissingDatabaseException>(() =>databaseHelperTest.AddEmailNotificationAsync(
            "test@test.com",
            "exampleSubject",
            "body",
            "status",
            new DateTime()
        ));
        return Task.CompletedTask;
    }
    [Test]
    public Task GetAllEmailsNotificationAsync_DatabaseNotFound_ThrowsMissingDatabaseException()
    {
        // Arrange: Use an invalid connection string to trigger a MySqlException
        var invalidConnectionString = "Server=localhost;Database=fake_db;User=root;Password=wrong_password;";
        var service = new DatabaseHelper(invalidConnectionString);

        // Act & Assert: Check that the correct exception is thrown
        var exception = Assert.ThrowsAsync<MissingDatabaseException>(
            async () => await service.GetAllEmailsNotificationAsync());

        // Verify the exception message is as expected
        Assert.That(exception.Message, Is.EqualTo("Could not find the database"));
        return Task.CompletedTask;
    }
    [Test]
    public Task MoveEmailNotificationToEmailsAsync_DatabaseConnectionFailure_ThrowsMissingDatabaseException()
    {
        // Arrange: Use an invalid connection string
        var invalidConnectionString = "Server=invalid_server;Database=invalid_db;User=root;Password=wrong_password;";
        var service = new DatabaseHelper(invalidConnectionString); // Your service class

        // Act & Assert: Expect a MissingDatabaseException to be thrown
        var exception = Assert.ThrowsAsync<MissingDatabaseException>(
            async () => await service.MoveEmailNotificationToEmailsAsync(1, "Test Status"));

        // Verify the exception message is as expected
        Assert.That(exception.Message.Substring(0, 36), Is.EqualTo("Could not find the database. Error: ")); // Validate prefix of error message
        return Task.CompletedTask;
    }
    
    
}