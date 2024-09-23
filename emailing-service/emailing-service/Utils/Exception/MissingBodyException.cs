namespace emailing_service.Utils.Exception;

public class MissingBodyException : System.Exception
{
    public MissingBodyException() { }

    public MissingBodyException(string message) 
        : base(message) { }

    public MissingBodyException(string message, System.Exception inner) 
        : base(message, inner) { }
}