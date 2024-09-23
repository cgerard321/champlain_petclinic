namespace emailing_service.Utils.Exception;

public class EmailStringContainsPlaceholder :System.Exception
{
    public EmailStringContainsPlaceholder() { }

    public EmailStringContainsPlaceholder(string message, string placeHolder) 
        : base("Could not build message because field " + message + " contains an empty placeholder : " +  placeHolder) { }

    public EmailStringContainsPlaceholder(string message, System.Exception inner) 
        : base(message, inner) { }
}