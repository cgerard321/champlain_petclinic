
using emailing_service.Utils.Exception;

namespace emailing_service_test.Utils.Exception;
[TestFixture]
public class CreatedAlreadyExistingTemplateTests
{
    [Test]
    public void Constructor_NoArguments_ShouldInstantiateException()
    {
        // Act
        var exception = new CreatedAlreadyExistingTemplate();

        // Assert
        Assert.IsNotNull(exception);
        Assert.IsInstanceOf<CreatedAlreadyExistingTemplate>(exception);
    }

    [Test]
    public void Constructor_WithMessage_ShouldSetMessage()
    {
        // Arrange
        string expectedMessage = "Template already exists.";

        // Act
        var exception = new CreatedAlreadyExistingTemplate(expectedMessage);

        // Assert
        Assert.That(exception.Message, Is.EqualTo(expectedMessage));
    }

    [Test]
    public void Constructor_WithMessageAndInnerException_ShouldSetProperties()
    {
        // Arrange
        string expectedMessage = "Template already exists.";
        var innerException = new InvalidOperationException("Inner exception");

        // Act
        var exception = new CreatedAlreadyExistingTemplate(expectedMessage, innerException);

        // Assert
        Assert.That(exception.Message, Is.EqualTo(expectedMessage));
        Assert.That(exception.InnerException, Is.EqualTo(innerException));
    }
}