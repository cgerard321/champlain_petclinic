using emailing_service.Models.Database;
using emailing_service.Utils.Exception;

namespace emailing_service_test.Models.Database;
[TestFixture]
public class DatabaseHelperTest
{
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
    
}