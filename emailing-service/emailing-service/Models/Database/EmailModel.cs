namespace emailing_service.Models.Database;

public class EmailModel
{
    public int Id { get; set; }
    public string Email { get; set; }
    public string Subject { get; set; }
    public string Body { get; set; }
    public string EmailStatus { get; set; }
}