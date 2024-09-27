using emailing_service.Models;
namespace emailing_service_test.Models;
[TestFixture]
public class ConnectionEmailServerTest
{
    
    [Test]
    [TestCase("smtp.mailersend.net",587,"test@example.com","secret","test@example.com","Test User")]
    [TestCase("smtp.sender.net",800,"test2@example.com","secret2","example2@example.com","Test User2    ")]
    [TestCase("mailer.send.net",600,"xilef@example.com","example3","example3@example.com","Example3")]
    public void ToString_ReturnsExpectedString(string smtpServerP,int portP,string usernameP,string passwordP,string emailP,string displayNameP)
    {
        // Arrange
        var smtpServer = smtpServerP;
        var port = portP;
        var username = usernameP;
        var password = passwordP; // This will not be printed
        var email = emailP;
        var displayName = displayNameP;
        var connectionEmailServer = new ConnectionEmailServer(smtpServer, port, username, password, email, displayName);

        // Act
        var result = connectionEmailServer.ToString();

        // Assert
        Assert.That(result, Is.EqualTo($"SMTP Server: {smtpServer}\n" +
                                       $"Port: {port}\n" +
                                       $"Username: {username}\n" +
                                       $"Email: {email}\n" +
                                       $"Display Name: {displayName}"));
    }
}