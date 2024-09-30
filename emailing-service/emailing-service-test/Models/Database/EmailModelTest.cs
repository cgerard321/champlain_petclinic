using emailing_service.Models.Database;

namespace emailing_service_test.Models.Database;

public class EmailModelTests
{
    [Test]
    public void Constructor_SetsPropertiesCorrectly()
    {
        // Arrange
        int id = 1;
        string email = "test@example.com";
        string subject = "Test Subject";
        string body = "Test Body";
        string emailStatus = "Test Status";

        // Act
        EmailModel emailModel = new EmailModel
        {
            Id = id,
            Email = email,
            Subject = subject,
            Body = body,
            EmailStatus = emailStatus
        };

        // Assert
        Assert.That(emailModel.Id, Is.EqualTo(id));
        Assert.That(emailModel.Email, Is.EqualTo(email));
        Assert.That(emailModel.Subject, Is.EqualTo(subject));
        Assert.That(emailModel.Body, Is.EqualTo(body));
        Assert.That(emailModel.EmailStatus, Is.EqualTo(emailStatus));
    }

    [Test]
    public void ToString_ReturnsCorrectString()
    {
        // Arrange
        EmailModel emailModel = new EmailModel
        {
            Id = 1,
            Email = "test@example.com",
            Subject = "Test Subject",
            Body = "Test Body",
            EmailStatus = "Test Status"
        };

        // Act
        string toString = emailModel.ToString();

        // Assert
        Assert.That(toString, Is.EqualTo($"Id: 1, Email: test@example.com, Subject: Test Subject, Body: Test Body, EmailStatus: Test Status"));
    }
}