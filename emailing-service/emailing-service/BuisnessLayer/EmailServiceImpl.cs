using emailing_service.Models;
using emailing_service.Models.Database;
using emailing_service.Models.EmailType;
using emailing_service.Utils;
using emailing_service.Utils.Exception;

namespace emailing_service.BuisnessLayer;

public class EmailServiceImpl : IEmailService
{
    private IDatabaseHelper _databaseHelper = new DatabaseHelper();
    
    
    
    public void SetDatabaseHelper(IDatabaseHelper databaseHelper)
    {
        _databaseHelper = databaseHelper;
    }
    public List<EmailModel> GetAllEmails()
    {
        try
        {
            return _databaseHelper.GetAllEmailsAsync().Result;
        }
        //We have no choice but to receive as Aggregate Exception since it is an async service, and it uses a possibly offline database.
        //What's more, mysql is closed during testing
        catch (AggregateException e)
        {
            Console.WriteLine(e);
            throw new MissingDatabaseException();
        }
    }

    public List<EmailModelNotification> GetAllNotificationEmails()
    {
        try
        {
            return _databaseHelper.GetAllEmailsNotificationAsync().Result;
        }
        //We have no choice but to receive as Aggregate Exception since it is an async service, and it uses a possibly offline database.
        //What's more, mysql is closed during testing
        catch (AggregateException e)
        {
            Console.WriteLine(e);
            throw new MissingDatabaseException();
        }
    }

    public OperationResult ReceiveHtml(string? templateName, string? htmlBody)
    {
        if (string.IsNullOrWhiteSpace(templateName))
            throw new TemplateFormatException($"Template Name is required. [{templateName}] is not valid as a template name.");
        if (string.IsNullOrWhiteSpace(htmlBody))
            throw new TemplateFormatException("HTML content was missing");
        if (EmailUtils.EmailTemplates.FirstOrDefault(e => e.Name == templateName) != null)
            throw new CreatedAlreadyExistingTemplate($"Template [{templateName}] already exists.");

        EmailUtils.EmailTemplates.Add(new EmailTemplate(templateName, htmlBody));

        return new OperationResult
        {
            IsSuccess = true,
            Message = $"Successfully created template [{templateName}] with HTML content."
        };
    }

    public OperationResult SendEmail(DirectEmailModel model)
    {
        Console.WriteLine("Received Email Call Function!");
        DirectEmailModel directEmailModel = model;
        Console.WriteLine("Found the model!" + directEmailModel);
        
        if (String.IsNullOrWhiteSpace(directEmailModel.EmailToSendTo))
            throw new BadEmailModel("Email To Send To is null or whitespace. EMAIL IS REQUIRED");
        if(!EmailUtils.CheckIfEmailIsValid(directEmailModel.EmailToSendTo))
            throw new BadEmailModel("Email To Send To Not Valid");
        if(String.IsNullOrWhiteSpace(directEmailModel.EmailTitle))
            throw new BadEmailModel("Email Title is null or whitespace");
        if (String.IsNullOrWhiteSpace(directEmailModel.TemplateName))
            directEmailModel.TemplateName = "Default";
        EmailTemplate? emailTemplate = EmailUtils.EmailTemplates.FirstOrDefault(e => e.Name == directEmailModel.TemplateName);
        if (emailTemplate == null)
            throw new TriedToFindNonExistingTemplate(
                $"Template {directEmailModel.TemplateName} does not exist. Please create a template first or use the default one (Default)");
        string builtEmail;
        try
        {
            string header = "";
            string body = "";
            string footer = "";
            string correspondentName = "";
            string sender = "";
            if (!String.IsNullOrWhiteSpace(directEmailModel.Header))
                header = directEmailModel.Header;
            if (!String.IsNullOrWhiteSpace(directEmailModel.Body))
                body = directEmailModel.Body;
            if (!String.IsNullOrWhiteSpace(directEmailModel.Footer))
                footer = directEmailModel.Footer;
            if (!String.IsNullOrWhiteSpace(directEmailModel.CorrespondantName))
                correspondentName = directEmailModel.CorrespondantName;
            if (!String.IsNullOrWhiteSpace(directEmailModel.SenderName))
                sender = directEmailModel.SenderName;
            builtEmail = emailTemplate.BuildEmail(
                header,
                body,
                footer,
                correspondentName,
                sender
            );
        }
        /*catch (NullReferenceException e)
        {
            Console.WriteLine(e);
            throw;
        }*/
        
        catch (TemplateRequiredFieldNotSet e)
        {
            Console.WriteLine(e);
            throw;
        }
        catch (TriedToFillEmailFieldWithEmptyWhiteSpace e)
        {
            Console.WriteLine(e);
            throw;
        }
        // Run the email sending task in a separate thread
        Task.Run(async () =>
        {
            try
            {
                var sendEmailResult = await EmailUtils.SendEmailAsync(
                    directEmailModel.EmailToSendTo,
                    directEmailModel.EmailTitle,
                    builtEmail,
                    EmailUtils.smtpClient
                );
                
                if (sendEmailResult.Status == "Sent")
                {
                    // Add the email to the database with a status of "Sent"
                    var databaseHelper = new DatabaseHelper();
                    await databaseHelper.AddEmailAsync(
                        directEmailModel.EmailToSendTo,
                        directEmailModel.EmailTitle,
                        builtEmail,
                        "Sent"
                    );
                }
                else
                {
                    // Add the email to the database with a status of "Failed"
                    var databaseHelper = new DatabaseHelper();
                    await databaseHelper.AddEmailAsync(
                        directEmailModel.EmailToSendTo,
                        directEmailModel.EmailTitle,
                        builtEmail,
                        "Failed"
                    );
                }
            }
            catch (Exception e)
            {
                Console.WriteLine(e);
            }
        });

        return new OperationResult
        {
            IsSuccess = true,
            Message = $"Successfully sent an email!"
        };
    }
    public OperationResult SendEmailNotification(NotificationEmailModel model)
    {
        Console.WriteLine("Received Email Call Function!");
        NotificationEmailModel directEmailModel = model;
        Console.WriteLine("Found the model!" + directEmailModel);
        if (String.IsNullOrWhiteSpace(directEmailModel.EmailToSendTo))
            throw new BadEmailModel("Email To Send To is null or whitespace. EMAIL IS REQUIRED");
        if(!EmailUtils.CheckIfEmailIsValid(directEmailModel.EmailToSendTo))
            throw new BadEmailModel("Email To Send To Not Valid");
        if(String.IsNullOrWhiteSpace(directEmailModel.EmailTitle))
            throw new BadEmailModel("Email Title is null or whitespace");
        if (String.IsNullOrWhiteSpace(directEmailModel.TemplateName))
            directEmailModel.TemplateName = "Default";
        //This is to handle the time as UTC-4
        if (directEmailModel.SentDate < TimeZoneInfo.ConvertTimeFromUtc(DateTime.UtcNow, TimeZoneInfo.CreateCustomTimeZone("UTC-4", new TimeSpan(-4, 0, 0), "UTC-4", "UTC-4")))
            throw new AlreadyPassedDate($"The Date for the notification  was already passed! {directEmailModel.SentDate} is higher than {DateTime.Now}");
        EmailTemplate? emailTemplate = EmailUtils.EmailTemplates.FirstOrDefault(e => e.Name == directEmailModel.TemplateName);
        if (emailTemplate == null)
            throw new TriedToFindNonExistingTemplate(
                $"Template {directEmailModel.TemplateName} does not exist. Please create a template first or use the default one (Default)");
        string builtEmail;
        try
        {
            string header = "";
            string body = "";
            string footer = "";
            string correspondentName = "";
            string sender = "";
            if (!String.IsNullOrWhiteSpace(directEmailModel.Header))
                header = directEmailModel.Header;
            if (!String.IsNullOrWhiteSpace(directEmailModel.Body))
                body = directEmailModel.Body;
            if (!String.IsNullOrWhiteSpace(directEmailModel.Footer))
                footer = directEmailModel.Footer;
            if (!String.IsNullOrWhiteSpace(directEmailModel.CorrespondantName))
                correspondentName = directEmailModel.CorrespondantName;
            if (!String.IsNullOrWhiteSpace(directEmailModel.SenderName))
                sender = directEmailModel.SenderName;
            builtEmail = emailTemplate.BuildEmail(
                header,
                body,
                footer,
                correspondentName,
                sender
            );
        }
        /*catch (NullReferenceException e)
        {
            Console.WriteLine(e);
            throw;
        }*/
        
        catch (TemplateRequiredFieldNotSet e)
        {
            Console.WriteLine(e);
            throw;
        }
        catch (TriedToFillEmailFieldWithEmptyWhiteSpace e)
        {
            Console.WriteLine(e);
            throw;
        }
        // Run the email sending task in a separate thread
        Task.Run(async () =>
        {
            try
            {
                // Add the email to the database with a status of "Sent"
                await _databaseHelper.AddEmailNotificationAsync(directEmailModel.EmailToSendTo, 
                        directEmailModel.EmailTitle, 
                        builtEmail , 
                        "NotSent", 
                        directEmailModel.SentDate
                    );
                    
            }
            catch (Exception e)
            {
                Console.WriteLine(e);
            }
        });

        return new OperationResult
        {
            IsSuccess = true,
            Message = $"Successfully Added email to waiting list!"
        };
    }

    public OperationResult SendRawEmail(RawEmailModel model)
    {
        RawEmailModel directEmailModel = model;
        Console.WriteLine("Found the model!" + directEmailModel);
        if (String.IsNullOrWhiteSpace(directEmailModel.EmailToSendTo))
            throw new BadEmailModel("Email To Send To is null or whitespace. EMAIL IS REQUIRED");
        if(!EmailUtils.CheckIfEmailIsValid(directEmailModel.EmailToSendTo))
            throw new BadEmailModel("Email To Send To Not Valid");
        if(String.IsNullOrWhiteSpace(directEmailModel.EmailTitle))
            throw new BadEmailModel("Email Title is null or whitespace");
        
        Task.Run(async () =>
        {
            try
            {
                var sendEmailResult = await EmailUtils.SendEmailAsync(
                    directEmailModel.EmailToSendTo,
                    directEmailModel.EmailTitle,
                    directEmailModel.Body,
                    EmailUtils.smtpClient
                );
                
                if (sendEmailResult.Status == "Sent")
                {
                    // Add the email to the database with a status of "Sent"
                    var databaseHelper = new DatabaseHelper();
                    await databaseHelper.AddEmailAsync(
                        directEmailModel.EmailToSendTo,
                        directEmailModel.EmailTitle,
                        directEmailModel.Body,
                        "Sent"
                    );
                }
                else
                {
                    // Add the email to the database with a status of "Failed"
                    var databaseHelper = new DatabaseHelper();
                    await databaseHelper.AddEmailAsync(
                        directEmailModel.EmailToSendTo,
                        directEmailModel.EmailTitle,
                        directEmailModel.Body,
                        "Failed"
                    );
                }
            }
            catch (Exception e)
            {
                Console.WriteLine(e);
            }
        });
        return new OperationResult
        {
            IsSuccess = true,
            Message = $"Successfully sent an email!"
        };
    }

    /*
    public OperationResult SendReminderEmail(ReminderEmailModel model)
    {
        ReminderEmailModel reminderEmailModel = model;
        Console.WriteLine("Found the model!" + reminderEmailModel);

        if (String.IsNullOrWhiteSpace(reminderEmailModel.EmailToSendTo))
            throw new BadEmailModel("Email To Send To is null or whitespace. EMAIL IS REQUIRED");
        if (!EmailUtils.CheckIfEmailIsValid(reminderEmailModel.EmailToSendTo))
            throw new BadEmailModel("Email To Send To Not Valid");
        if (String.IsNullOrWhiteSpace(reminderEmailModel.EmailTitle))
            throw new BadEmailModel("Email Title is null or whitespace");

        DateTime sendDate = reminderEmailModel.VisitDate.AddDays(-1);
        if (sendDate < DateTime.Now)
            throw new InvalidOperationException("The appointment date is too soon to send a reminder email.");

        Task.Run(async () =>
        {
            try
            {
                // Wait until the send date
                TimeSpan delay = sendDate - DateTime.Now;
                await Task.Delay(delay);

                var sendEmailResult = await EmailUtils.SendEmailAsync(
                    reminderEmailModel.EmailToSendTo,
                    reminderEmailModel.EmailTitle,
                    reminderEmailModel.Body,
                    EmailUtils.smtpClient
                );

                var databaseHelper = new DatabaseHelper();
                if (sendEmailResult.Status == "Sent")
                {
                    await databaseHelper.AddEmailAsync(
                        reminderEmailModel.EmailToSendTo,
                        reminderEmailModel.EmailTitle,
                        reminderEmailModel.Body,
                        "Sent"
                    );
                }
                else
                {
                    await databaseHelper.AddEmailAsync(
                        reminderEmailModel.EmailToSendTo,
                        reminderEmailModel.EmailTitle,
                        reminderEmailModel.Body,
                        "Failed"
                    );
                }
            }
            catch (Exception e)
            {
                Console.WriteLine(e);
            }
        });

        return new OperationResult
        {
            IsSuccess = true,
            Message = $"Successfully scheduled a reminder email!"
        };
    }
    */
}