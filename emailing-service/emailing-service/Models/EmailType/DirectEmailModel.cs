namespace emailing_service.Models.EmailType;

public class DirectEmailModel
{
    public DirectEmailModel()
    {
        
    }

    public DirectEmailModel(string emailToSendTo, string emailTitle, string templateName, string header, string body, string footer, string correspondantName, string senderName)
    {
        EmailToSendTo = emailToSendTo;
        EmailTitle = emailTitle;
        TemplateName = templateName;
        Header = header;
        Body = body;
        Footer = footer;
        CorrespondantName = correspondantName;
        SenderName = senderName;
    }

    public string EmailToSendTo { get; set; }
    public string EmailTitle { get; set; }
    public string TemplateName { get; set; }
    public string Header {get; set;}
    public string Body { get; set; }
    public string Footer { get; set; }
    public string CorrespondantName { get; set; }
    public string SenderName { get; set; }
    
    public bool IsEmpty()
    {
        return GetType().GetProperties().All(p => p.GetValue(this) == null);
    }
}