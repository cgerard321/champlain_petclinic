using emailing_service.Utils.Exception;

namespace emailing_service_test.Utils.Exception;

[TestFixture]
public class TriedToFindNonExistingTemplateTests
{
    [Test]
    public void Constructor_NoArguments_ShouldInstantiateException()
    {
        // Act
        var exception = new TriedToFindNonExistingTemplate();

        // Assert
        Assert.IsNotNull(exception);
        Assert.IsInstanceOf<TriedToFindNonExistingTemplate>(exception);
    }

    [Test]
    public void Constructor_WithMessage_ShouldSetMessage()
    {
        // Arrange
        string expectedMessage = "Template not found.";

        // Act
        var exception = new TriedToFindNonExistingTemplate(expectedMessage);

        // Assert
        Assert.AreEqual(expectedMessage, exception.Message);
    }

    [Test]
    public void Constructor_WithMessageAndInnerException_ShouldSetProperties()
    {
        // Arrange
        string expectedMessage = "An error occurred while searching for the template.";
        var innerException = new InvalidOperationException("Inner exception");

        // Act
        var exception = new TriedToFindNonExistingTemplate(expectedMessage, innerException);

        // Assert
        Assert.AreEqual(expectedMessage, exception.Message);
        Assert.AreEqual(innerException, exception.InnerException);
    }
}