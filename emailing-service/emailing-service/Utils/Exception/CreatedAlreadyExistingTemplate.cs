namespace emailing_service.Utils.Exception;

public class CreatedAlreadyExistingTemplate :System.Exception
{
    public CreatedAlreadyExistingTemplate() { }

    public CreatedAlreadyExistingTemplate(string message) 
        : base(message) { }

    public CreatedAlreadyExistingTemplate(string message, System.Exception inner) 
        : base(message, inner) { }
}