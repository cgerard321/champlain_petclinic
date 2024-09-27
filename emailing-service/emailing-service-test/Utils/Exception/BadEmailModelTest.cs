using emailing_service.Utils.Exception;


namespace emailing_service_test.Utils.Exception;
[TestFixture]
public class BadEmailModelTest
{
    [Test]
    public void Constructor_NoArguments_ShouldInstantiateException()
    {
        // Act
        var exception = new BadEmailModel();

        // Assert
        Assert.IsNotNull(exception);
        Assert.IsInstanceOf<BadEmailModel>(exception);
    }

    [Test]
    public void Constructor_WithMessage_ShouldSetMessage()
    {
        // Arrange
        string expectedMessage = "Invalid email model.";

        // Act
        var exception = new BadEmailModel(expectedMessage);

        // Assert
        Assert.That(exception.Message, Is.EqualTo(expectedMessage));
    }

    [Test]
    public void Constructor_WithMessageAndInnerException_ShouldSetProperties()
    {
        // Arrange
        string expectedMessage = "Invalid email model.";
        var innerException = new InvalidOperationException("Inner exception");

        // Act
        var exception = new BadEmailModel(expectedMessage, innerException);

        // Assert
        Assert.That(exception.Message, Is.EqualTo(expectedMessage));
        Assert.That(exception.InnerException, Is.EqualTo(innerException));
    }
}