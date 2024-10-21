namespace emailing_service.Models.EmailType;

public class RawEmailModel
{
    public RawEmailModel()
    {
        
    }

    public RawEmailModel(string emailToSendTo, string emailTitle, string body )
    {
        EmailToSendTo = emailToSendTo;
        EmailTitle = emailTitle;
        Body = body;
    }

    public string EmailToSendTo { get; set; }
    public string EmailTitle { get; set; }
    
    public string Body { get; set; }
    
    
    public bool IsEmpty()
    {
        return GetType().GetProperties().All(p => p.GetValue(this) == null);
    }
}