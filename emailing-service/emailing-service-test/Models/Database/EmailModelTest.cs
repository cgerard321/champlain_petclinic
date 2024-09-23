using emailing_service.Models.Database;
using NUnit.Framework;

namespace emailing_service_test;

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
        Assert.AreEqual(id, emailModel.Id);
        Assert.AreEqual(email, emailModel.Email);
        Assert.AreEqual(subject, emailModel.Subject);
        Assert.AreEqual(body, emailModel.Body);
        Assert.AreEqual(emailStatus, emailModel.EmailStatus);
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
        Assert.AreEqual($"Id: 1, Email: test@example.com, Subject: Test Subject, Body: Test Body, EmailStatus: Test Status", toString);
    }
}