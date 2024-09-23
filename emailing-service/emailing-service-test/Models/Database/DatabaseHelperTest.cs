using System.Threading.Tasks;
using emailing_service.Models.Database;
using emailing_service.Utils.Exception;
using MySqlConnector;
using NUnit.Framework;

namespace emailing_service_test;
[TestFixture]
public class DatabaseHelperTest
{
    [Test]
    public async Task CreateTableAsync_ThrowsException_WhenDatabaseDoesNotExist()
    {
        // Arrange
        IDatabaseHelper databaseHelperTest = new DatabaseHelper();
        // Act and Assert
        Assert.ThrowsAsync<MissingDatabaseException>(() =>databaseHelperTest.CreateTableAsync(1));
    }
    [Test]
    public async Task AddEmailAsync_ThrowsException_WhenDatabaseDoesNotExist()
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
    }
    
}