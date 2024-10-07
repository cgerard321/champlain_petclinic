namespace emailing_service.Models.Database;

public class EmailModelNotification
{
    public int Id { get; set; }
    public string Email { get; set; }
    public string Subject { get; set; }
    public string Body { get; set; }
    public string EmailStatus { get; set; }
    public DateTime SendTime { get; set; }
    
    public override string ToString()
    {
        return $"Id: {Id}, Email: {Email}, Subject: {Subject}, Body: {Body}, EmailStatus: {EmailStatus}, SendTime: {SendTime}";
    }
}