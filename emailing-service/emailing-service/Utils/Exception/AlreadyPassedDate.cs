namespace emailing_service.Utils.Exception;

public class AlreadyPassedDate : System.Exception
{
    public AlreadyPassedDate() { }

    public AlreadyPassedDate(string message) 
        : base(message) { }

    public AlreadyPassedDate(string message, System.Exception inner) 
        : base(message, inner) { }
}