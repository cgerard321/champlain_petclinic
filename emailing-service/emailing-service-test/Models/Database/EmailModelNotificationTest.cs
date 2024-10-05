using emailing_service.Models.Database;

namespace emailing_service_test.Models.Database;

[TestFixture]
public class EmailModelNotificationTest
{
    [Test]
    public void EmailModelNotification_Properties_SetAndGetValues()
    {
        // Arrange
        var emailNotification = new EmailModelNotification
        {
            Id = 1,
            Email = "test@example.com",
            Subject = "Test Subject",
            Body = "This is a test email body.",
            EmailStatus = "Sent",
            SendTime = DateTime.Now
        };

        // Act & Assert
        Assert.That(emailNotification.Id, Is.EqualTo(1));
        Assert.That(emailNotification.Email, Is.EqualTo("test@example.com"));
        Assert.That(emailNotification.Subject, Is.EqualTo("Test Subject"));
        Assert.That(emailNotification.Body, Is.EqualTo("This is a test email body."));
        Assert.That(emailNotification.EmailStatus, Is.EqualTo("Sent"));
        Assert.IsTrue(emailNotification.SendTime <= DateTime.Now); // Check that SendTime is less than or equal to now
    }

    [Test]
    public void EmailModelNotification_ToString_ReturnsExpectedFormat()
    {
        // Arrange
        var sendTime = DateTime.Now;
        var emailNotification = new EmailModelNotification
        {
            Id = 2,
            Email = "test2@example.com",
            Subject = "Another Test Subject",
            Body = "This is another test email body.",
            EmailStatus = "Pending",
            SendTime = sendTime
        };

        // Act
        var result = emailNotification.ToString();

        // Assert
        var expectedOutput = $"Id: 2, Email: test2@example.com, Subject: Another Test Subject, Body: This is another test email body., EmailStatus: Pending, SendTime: {sendTime}";
        Assert.IsTrue(result.StartsWith("Id: 2, Email: test2@example.com,")); // Check the start of the output
        Assert.IsTrue(result.EndsWith($"SendTime: {sendTime}")); // Check the end of the output
    }
}