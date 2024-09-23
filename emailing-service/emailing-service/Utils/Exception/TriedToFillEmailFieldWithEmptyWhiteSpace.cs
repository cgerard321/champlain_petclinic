namespace emailing_service.Utils.Exception;

public class TriedToFillEmailFieldWithEmptyWhiteSpace : System.Exception
{
    public TriedToFillEmailFieldWithEmptyWhiteSpace() { }

    public TriedToFillEmailFieldWithEmptyWhiteSpace(string message, string fieldName) 
        : base("The value of --> " + message + " <-- is not assignable for the " +fieldName + " of the email") { }

    public TriedToFillEmailFieldWithEmptyWhiteSpace(string message, System.Exception inner) 
        : base(message, inner) { }
    
}