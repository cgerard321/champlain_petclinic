using emailing_service.Utils.Exception;

namespace emailing_service_test.Utils.Exception;

[TestFixture]
public class AlreadyPassedDateTest
{
    [Test]
    public void AlreadyPassedDate_DefaultConstructor_InitializesException()
    {
        var exception = new AlreadyPassedDate();

        // Assert
        Assert.IsNotNull(exception);
        Assert.IsInstanceOf<AlreadyPassedDate>(exception);
    }

    [Test]
    public void AlreadyPassedDate_MessageConstructor_InitializesExceptionWithMessage()
    {
        // Arrange
        var message = "This date has already passed.";

        // Act
        var exception = new AlreadyPassedDate(message);

        // Assert
        Assert.IsNotNull(exception);
        Assert.That(exception.Message, Is.EqualTo(message));
        Assert.IsNull(exception.InnerException);
    }

    [Test]
    public void AlreadyPassedDate_MessageInnerExceptionConstructor_InitializesException()
    {
        // Arrange
        var message = "This date has already passed.";
        var innerException = new System.Exception("Inner exception details.");

        // Act
        var exception = new AlreadyPassedDate(message, innerException);

        // Assert
        Assert.IsNotNull(exception);
        Assert.That(exception.Message, Is.EqualTo(message));
        Assert.That(exception.InnerException, Is.EqualTo(innerException));
    }
}