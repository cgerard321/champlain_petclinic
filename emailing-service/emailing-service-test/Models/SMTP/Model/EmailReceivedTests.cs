using emailing_service.Models.SMTP.Model;

namespace emailing_service_test.Models.SMTP.Model;

using NUnit.Framework;
using System;

[TestFixture]
public class EmailReceivedTests
{
    [Test]
    public void EmailReceived_Constructor_ShouldInitializeProperties()
    {
        // Arrange
        string from = "sender@example.com";
        string subject = "Test Subject";
        DateTime dateReceived = DateTime.Now;
        string plainTextBody = "This is a test email body.";

        // Act
        var emailReceived = new EmailReceived(from, subject, dateReceived, plainTextBody);

        // Assert
        Assert.AreEqual(from, emailReceived.From);
        Assert.AreEqual(subject, emailReceived.Subject);
        Assert.AreEqual(dateReceived, emailReceived.DateReceived);
        Assert.AreEqual(plainTextBody, emailReceived.PlainTextBody);
    }

    [Test]
    public void EmailReceived_SetAndGetProperties_ShouldWorkCorrectly()
    {
        // Arrange
        var emailReceived = new EmailReceived("initial@example.com", "Initial Subject", DateTime.Now, "Initial Body");

        // Act
        emailReceived.From = "updated@example.com";
        emailReceived.Subject = "Updated Subject";
        emailReceived.DateReceived = DateTime.Now.AddDays(-1);
        emailReceived.PlainTextBody = "Updated Body";

        // Assert
        Assert.AreEqual("updated@example.com", emailReceived.From);
        Assert.AreEqual("Updated Subject", emailReceived.Subject);
        Assert.IsTrue(emailReceived.DateReceived < DateTime.Now); // Assuming you want to check if the date is before now
        Assert.AreEqual("Updated Body", emailReceived.PlainTextBody);
    }

    [Test]
    public void EmailReceived_EmptyConstructor_ShouldNotThrowException()
    {
        // Arrange & Act
        var emailReceived = new EmailReceived(string.Empty, string.Empty, DateTime.MinValue, string.Empty);

        // Assert
        Assert.IsNotNull(emailReceived);
    }
}
