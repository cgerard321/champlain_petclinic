using emailing_service.Utils.Exception;

namespace emailing_service_test.Utils.Exception;

public class MissingBodyExceptionTest
{
    [TestFixture]
    public class MissingBodyExceptionTests
    {
        [Test]
        public void Constructor_NoArguments_ShouldInstantiateException()
        {
            // Act
            var exception = new MissingBodyException();

            // Assert
            Assert.IsNotNull(exception);
            Assert.IsInstanceOf<MissingBodyException>(exception);
        }

        [Test]
        public void Constructor_WithMessage_ShouldSetMessage()
        {
            // Arrange
            string expectedMessage = "The body is missing.";

            // Act
            var exception = new MissingBodyException(expectedMessage);

            // Assert
            Assert.AreEqual(expectedMessage, exception.Message);
        }

        [Test]
        public void Constructor_WithMessageAndInnerException_ShouldSetProperties()
        {
            // Arrange
            string expectedMessage = "An error occurred due to missing body.";
            var innerException = new InvalidOperationException("Inner exception");

            // Act
            var exception = new MissingBodyException(expectedMessage, innerException);

            // Assert
            Assert.AreEqual(expectedMessage, exception.Message);
            Assert.AreEqual(innerException, exception.InnerException);
        }
    }
}