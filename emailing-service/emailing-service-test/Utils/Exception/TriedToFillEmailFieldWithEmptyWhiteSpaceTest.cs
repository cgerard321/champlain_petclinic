using emailing_service.Utils.Exception;

namespace emailing_service_test.Utils.Exception;

[TestFixture]
public class TriedToFillEmailFieldWithEmptyWhiteSpaceTests
{
    [Test]
    public void Constructor_NoArguments_ShouldInstantiateException()
    {
        // Act
        var exception = new TriedToFillEmailFieldWithEmptyWhiteSpace();

        // Assert
        Assert.IsNotNull(exception);
        Assert.IsInstanceOf<TriedToFillEmailFieldWithEmptyWhiteSpace>(exception);
    }

    [Test]
    public void Constructor_WithMessageAndFieldName_ShouldSetFormattedMessage()
    {
        // Arrange
        string expectedMessage = "Empty value";
        string expectedFieldName = "Email Address";
        string expectedFormattedMessage = "The value of --> Empty value <-- is not assignable for the Email Address of the email";

        // Act
        var exception = new TriedToFillEmailFieldWithEmptyWhiteSpace(expectedMessage, expectedFieldName);

        // Assert
        Assert.AreEqual(expectedFormattedMessage, exception.Message);
    }

    [Test]
    public void Constructor_WithMessageAndInnerException_ShouldSetProperties()
    {
        // Arrange
        string expectedMessage = "An error occurred while assigning a field.";
        var innerException = new InvalidOperationException("Inner exception");

        // Act
        var exception = new TriedToFillEmailFieldWithEmptyWhiteSpace(expectedMessage, innerException);

        // Assert
        Assert.AreEqual(expectedMessage, exception.Message);
        Assert.AreEqual(innerException, exception.InnerException);
    }
}