namespace emailing_service.Models.EmailType;

public class NotificationEmailModel
{
    public NotificationEmailModel()
    {
        
    }

    public NotificationEmailModel(string emailToSendTo, string emailTitle, string templateName, string header, string body, string footer, string correspondantName, string senderName, DateTime sentDate)
    {
        EmailToSendTo = emailToSendTo;
        EmailTitle = emailTitle;
        TemplateName = templateName;
        Header = header;
        Body = body;
        Footer = footer;
        CorrespondantName = correspondantName;
        SenderName = senderName;
        SentDate = sentDate;
    }

    public string EmailToSendTo { get; set; }
    public string EmailTitle { get; set; }
    public string TemplateName { get; set; }
    public string Header {get; set;}
    public string Body { get; set; }
    public string Footer { get; set; }
    public string CorrespondantName { get; set; }
    public string SenderName { get; set; }
    
    public DateTime SentDate { get; set; }
    
    
    public bool IsEmpty()
    {
        return GetType().GetProperties().All(p => p.GetValue(this) == null);
    }
}