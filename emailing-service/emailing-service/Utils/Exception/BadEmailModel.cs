namespace emailing_service.Utils.Exception;

public class BadEmailModel : System.Exception
{
    public BadEmailModel() { }

    public BadEmailModel(string message) 
        : base(message) { }

    public BadEmailModel(string message, System.Exception inner) 
        : base(message, inner) { }
    
}