using emailing_service.Models;


namespace emailing_service_test.Models;

[TestFixture]
public class OperationResultTests
{
    [Test]
    public void OperationResult_DefaultConstructor_IsSuccessFalseAndMessageNull()
    {
        // Arrange
        var operationResult = new OperationResult();

        // Assert
        Assert.IsFalse(operationResult.IsSuccess);
        Assert.IsNull(operationResult.Message);
    }

    [Test]
    public void OperationResult_SetIsSuccess_True_IsSuccessTrue()
    {
        // Arrange
        var operationResult = new OperationResult { IsSuccess = true };

        // Assert
        Assert.IsTrue(operationResult.IsSuccess);
    }

    [Test]
    public void OperationResult_SetMessage_MessageSet()
    {
        // Arrange
        var operationResult = new OperationResult { Message = "Test message" };

        // Assert
        Assert.That(operationResult.Message, Is.EqualTo("Test message"));
    }

    [Test]
    public void OperationResult_SetBothProperties_IsSuccessTrueAndMessageSet()
    {
        // Arrange
        var operationResult = new OperationResult { IsSuccess = true, Message = "Test message" };

        // Assert
        Assert.IsTrue(operationResult.IsSuccess);
        Assert.That(operationResult.Message, Is.EqualTo("Test message"));
    }
}