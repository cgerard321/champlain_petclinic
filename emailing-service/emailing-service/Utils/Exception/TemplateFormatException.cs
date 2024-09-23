namespace emailing_service.Utils.Exception;

public class TemplateFormatException :System.Exception
{
    public TemplateFormatException() { }

    public TemplateFormatException(string message) 
        : base(message) { }

    public TemplateFormatException(string message, System.Exception inner) 
        : base(message, inner) { }
}