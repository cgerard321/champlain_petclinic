namespace emailing_service.Models;

public class ConnectionEmailServer
{
    public string SmtpServer => _smtpServer;

    public int Port => _port;

    public string Username => _username;

    public string Password => _password;
    
    public string Email => _email;

    public string DisplayName => _displayName;
    
    private readonly string _smtpServer;
    private readonly int _port;
    private readonly string _username;
    private readonly string _password;
    private readonly string _email;
    private readonly string _displayName;

    public ConnectionEmailServer(string smtpServer, int port, string username, string password, string email, string displayName)
    {
        _smtpServer = smtpServer;
        _port = port;
        _username = username;
        _password = password;
        _email = email;
        _displayName = displayName;
    }
    public override string ToString()
    {
        // Avoid printing sensitive information such as passwords
        return $"SMTP Server: {_smtpServer}\n" +
               $"Port: {_port}\n" +
               $"Username: {_username}\n" +
               $"Email: {_email}\n" +
               $"Display Name: {_displayName}";
    }
}