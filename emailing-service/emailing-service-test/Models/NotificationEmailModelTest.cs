using emailing_service.Models.EmailType;

namespace emailing_service_test.Models;

[TestFixture]
public class NotificationEmailModelTest
{
    [Test]
    public void NotificationEmailModel_Constructor_InitializesProperties()
    {
        // Arrange
        var emailToSendTo = "test@example.com";
        var emailTitle = "Test Email";
        var templateName = "WelcomeTemplate";
        var header = "Welcome!";
        var body = "This is the body of the email.";
        var footer = "Best Regards,";
        var correspondantName = "John Doe";
        var senderName = "Jane Smith";
        var sentDate = DateTime.Now;

        // Act
        var notificationEmail = new NotificationEmailModel(emailToSendTo, emailTitle, templateName, header, body, footer, correspondantName, senderName, sentDate);

        // Assert
        Assert.That(notificationEmail.EmailToSendTo, Is.EqualTo(emailToSendTo));
        Assert.That(notificationEmail.EmailTitle, Is.EqualTo(emailTitle));
        Assert.That(notificationEmail.TemplateName, Is.EqualTo(templateName));
        Assert.That(notificationEmail.Header, Is.EqualTo(header));
        Assert.That(notificationEmail.Body, Is.EqualTo(body));
        Assert.That(notificationEmail.Footer, Is.EqualTo(footer));
        Assert.That(notificationEmail.CorrespondantName, Is.EqualTo(correspondantName));
        Assert.That(notificationEmail.SenderName, Is.EqualTo(senderName));
        Assert.That(notificationEmail.SentDate, Is.EqualTo(sentDate));
    }
    

    [Test]
    public void NotificationEmailModel_IsEmpty_SomePropertiesSet_ReturnsFalse()
    {
        // Arrange
        var notificationEmail = new NotificationEmailModel
        {
            EmailToSendTo = "test@example.com",
            // other properties are null
        };

        // Act
        var result = notificationEmail.IsEmpty();

        // Assert
        Assert.IsFalse(result);
    }

    [Test]
    public void NotificationEmailModel_IsEmpty_AllPropertiesSet_ReturnsFalse()
    {
        // Arrange
        var notificationEmail = new NotificationEmailModel
        {
            EmailToSendTo = "test@example.com",
            EmailTitle = "Test Email",
            TemplateName = "WelcomeTemplate",
            Header = "Welcome!",
            Body = "This is the body of the email.",
            Footer = "Best Regards,",
            CorrespondantName = "John Doe",
            SenderName = "Jane Smith",
            SentDate = DateTime.Now
        };
        // Act
        var result = notificationEmail.IsEmpty();

        // Assert
        Assert.IsFalse(result);
    }
}