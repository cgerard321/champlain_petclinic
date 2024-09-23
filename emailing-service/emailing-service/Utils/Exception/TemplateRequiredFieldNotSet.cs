namespace emailing_service.Utils.Exception;

public class TemplateRequiredFieldNotSet : System.Exception
{
    public TemplateRequiredFieldNotSet() { }

    public TemplateRequiredFieldNotSet(string message) 
        : base("Could not build message because field " + message + " is required in this template") { }

    public TemplateRequiredFieldNotSet(string message, System.Exception inner) 
        : base(message, inner) { }
}