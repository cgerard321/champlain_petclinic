using emailing_service.Utils.Exception;
namespace emailing_service_test.Utils.Exception;

[TestFixture]
public class AddedAnAlreadyPresentReminderTest
{
    [Test]
    public void Constructor_NoArguments_ShouldInstantiateException()
    {
        // Act
        var exception = new AddedAnAlreadyPresentReminder();

        // Assert
        Assert.IsNotNull(exception);
        Assert.IsInstanceOf<AddedAnAlreadyPresentReminder>(exception);
    }

    [Test]
    public void Constructor_WithMessage_ShouldSetMessage()
    {
        // Arrange
        string expectedMessage = "Reminder already exists.";

        // Act
        var exception = new AddedAnAlreadyPresentReminder(expectedMessage);

        // Assert
        Assert.That(exception.Message, Is.EqualTo(expectedMessage));
    }

    [Test]
    public void Constructor_WithMessageAndInnerException_ShouldSetProperties()
    {
        // Arrange
        string expectedMessage = "Reminder already exists.";
        var innerException = new InvalidOperationException("Inner exception");

        // Act
        var exception = new AddedAnAlreadyPresentReminder(expectedMessage, innerException);

        // Assert
        Assert.That(exception.Message, Is.EqualTo(expectedMessage));
        Assert.That(exception.InnerException, Is.EqualTo(innerException));
    }
}

