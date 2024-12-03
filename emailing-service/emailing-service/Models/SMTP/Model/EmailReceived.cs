namespace emailing_service.Models.SMTP.Model;

public class EmailReceived
{

    public string From { get; set; }        // The email address of the sender
    public string Subject { get; set; }     // The subject line of the email
    public DateTime DateReceived { get; set; }      // The date and time the email was sent
    public string PlainTextBody { get; set; } // The main text content of the email

    // Optionally, you can add a constructor for easier instantiation
    public EmailReceived(string from, string subject, DateTime date, string plainTextBody)
    {
        From = from;
        Subject = subject;
        DateReceived = date;
        PlainTextBody = plainTextBody;
    }
}