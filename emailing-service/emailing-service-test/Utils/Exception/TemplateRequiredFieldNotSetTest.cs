
using emailing_service.Utils.Exception;


namespace emailing_service_test.Utils.Exception;

[TestFixture]
public class TemplateRequiredFieldNotSetTests
{
    [Test]
    public void Constructor_NoArguments_ShouldInstantiateException()
    {
        // Act
        var exception = new TemplateRequiredFieldNotSet();

        // Assert
        Assert.IsNotNull(exception);
        Assert.IsInstanceOf<TemplateRequiredFieldNotSet>(exception);
    }

    [Test]
    public void Constructor_WithMessage_ShouldSetFormattedMessage()
    {
        // Arrange
        string expectedField = "Subject";
        string expectedMessage = "Could not build message because field Subject is required in this template";

        // Act
        var exception = new TemplateRequiredFieldNotSet(expectedField);

        // Assert
        Assert.That(exception.Message, Is.EqualTo(expectedMessage));
    }

    [Test]
    public void Constructor_WithMessageAndInnerException_ShouldSetProperties()
    {
        // Arrange
        string expectedMessage = "An error occurred due to a missing required field.";
        var innerException = new InvalidOperationException("Inner exception");

        // Act
        var exception = new TemplateRequiredFieldNotSet(expectedMessage, innerException);

        // Assert
        Assert.That(exception.Message, Is.EqualTo(expectedMessage));
        Assert.That(exception.InnerException, Is.EqualTo(innerException));
    }
}