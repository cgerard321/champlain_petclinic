namespace emailing_service.Models.EmailType;

public class DirectEmailModel
{
    public string EmailToSendTo { get; set; }
    public string EmailTitle { get; set; }
    public string TemplateName { get; set; }
    public string Header {get; set;}
    public string Body { get; set; }
    public string Footer { get; set; }
    public string CorrespondantName { get; set; }
    public string SenderName { get; set; }
}