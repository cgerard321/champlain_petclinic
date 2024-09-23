namespace emailing_service.Utils.Exception;

public class MissingDatabaseException : System.Exception
{
    public MissingDatabaseException() { }

    public MissingDatabaseException(string message) 
        : base(message) { }

    public MissingDatabaseException(string message, System.Exception inner) 
        : base(message, inner) { }
}