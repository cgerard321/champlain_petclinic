using emailing_service.Utils.Exception;

namespace emailing_service_test.Utils.Exception;

[TestFixture]
public class EmailStringContainsPlaceholderTests
{
    [Test]
    public void Constructor_NoArguments_ShouldInstantiateException()
    {
        // Act
        var exception = new EmailStringContainsPlaceholder();

        // Assert
        Assert.IsNotNull(exception);
        Assert.IsInstanceOf<EmailStringContainsPlaceholder>(exception);
    }

    [Test]
    public void Constructor_WithMessageAndPlaceholder_ShouldSetFormattedMessage()
    {
        // Arrange
        string expectedField = "Subject";
        string expectedPlaceholder = "{{user_name}}";
        string expectedMessage = "Could not build message because field Subject contains an empty placeholder : {{user_name}}";

        // Act
        var exception = new EmailStringContainsPlaceholder(expectedField, expectedPlaceholder);

        // Assert
        Assert.AreEqual(expectedMessage, exception.Message);
    }

    [Test]
    public void Constructor_WithMessageAndInnerException_ShouldSetProperties()
    {
        // Arrange
        string expectedMessage = "An error occurred.";
        var innerException = new InvalidOperationException("Inner exception");

        // Act
        var exception = new EmailStringContainsPlaceholder(expectedMessage, innerException);

        // Assert
        Assert.AreEqual(expectedMessage, exception.Message);
        Assert.AreEqual(innerException, exception.InnerException);
    }
}