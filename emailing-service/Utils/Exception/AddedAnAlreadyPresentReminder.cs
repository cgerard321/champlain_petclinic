namespace emailing_system.Utils.Exception;

public class AddedAnAlreadyPresentReminder : System.Exception
{
    public AddedAnAlreadyPresentReminder() { }

    public AddedAnAlreadyPresentReminder(string message) 
        : base(message) { }

    public AddedAnAlreadyPresentReminder(string message, System.Exception inner) 
        : base(message, inner) { }
}