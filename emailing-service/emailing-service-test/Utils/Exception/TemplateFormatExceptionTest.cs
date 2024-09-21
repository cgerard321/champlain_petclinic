using emailing_service.Utils.Exception;

namespace emailing_service_test.Utils.Exception;

[TestFixture]
public class TemplateFormatExceptionTests
{
    [Test]
    public void Constructor_NoArguments_ShouldInstantiateException()
    {
        // Act
        var exception = new TemplateFormatException();

        // Assert
        Assert.IsNotNull(exception);
        Assert.IsInstanceOf<TemplateFormatException>(exception);
    }

    [Test]
    public void Constructor_WithMessage_ShouldSetMessage()
    {
        // Arrange
        string expectedMessage = "The template format is invalid.";

        // Act
        var exception = new TemplateFormatException(expectedMessage);

        // Assert
        Assert.AreEqual(expectedMessage, exception.Message);
    }

    [Test]
    public void Constructor_WithMessageAndInnerException_ShouldSetProperties()
    {
        // Arrange
        string expectedMessage = "An error occurred due to an invalid template format.";
        var innerException = new InvalidOperationException("Inner exception");

        // Act
        var exception = new TemplateFormatException(expectedMessage, innerException);

        // Assert
        Assert.AreEqual(expectedMessage, exception.Message);
        Assert.AreEqual(innerException, exception.InnerException);
    }
}