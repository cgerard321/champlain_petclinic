using emailing_service.Models;
using emailing_service.Models.EmailType;
using emailing_service.Utils;
using emailing_service.Utils.Exception;
using Microsoft.AspNetCore.Http.HttpResults;

namespace emailing_service.BuisnessLayer;

public class EmailServiceImpl : IEmailService
{
    public OperationResult ReceiveHtml(string? templateName, string? htmlBody)
    {
        if (string.IsNullOrWhiteSpace(templateName))
            throw new TemplateFormatException($"Template Name is required. [{templateName}] is not valid as a template name.");
        if (string.IsNullOrWhiteSpace(htmlBody))
            throw new TemplateFormatException("HTML content was missing");
        if (EmailUtils.EmailTemplates.FirstOrDefault(e => e.Name == templateName) != null)
            throw new TemplateFormatException($"Template [{templateName}] already exists.");

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
        if (model == null)
            throw new MissingBodyException("Email Model is null");
        DirectEmailModel directEmailModel = model;
        Console.WriteLine("Found the model!" + directEmailModel.ToString());
        
        
        if (String.IsNullOrWhiteSpace(directEmailModel.EmailToSendTo))
            throw new BadEmailModel("Email To Send To is null or whitespace. EMAIL IS REQUIRED");
        if(!EmailUtils.CheckIfEmailIsValid(directEmailModel.EmailToSendTo))
            throw new BadEmailModel("Email To Send To Not Valid");
        if(String.IsNullOrWhiteSpace(directEmailModel.EmailTitle))
            throw new BadEmailModel("Email Title is null or whitespace");
        if (directEmailModel.TemplateName == null)
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
            if (directEmailModel.Header != null)
                header = directEmailModel.Header;
            if (directEmailModel.Body != null)
                body = directEmailModel.Body;
            if (directEmailModel.Footer != null)
                footer = directEmailModel.Footer;
            if (directEmailModel.CorrespondantName != null)
                correspondentName = directEmailModel.CorrespondantName;
            if (directEmailModel.SenderName != null)
                sender = directEmailModel.SenderName;
            builtEmail = emailTemplate.BuildEmail(
                header,
                body,
                footer,
                correspondentName,
                sender
            );
        }
        catch (NullReferenceException e)
        {
            Console.WriteLine(e);
            throw;
        }
        catch (EmailStringContainsPlaceholder e)
        {
            Console.WriteLine(e);
            throw;
        }
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

        try
        {
            // Run the email sending task in a separate thread
            Task.Run(() =>
            {
                try
                {
                    EmailUtils.SendEmailAsync(
                        directEmailModel.EmailToSendTo,
                        directEmailModel.EmailTitle,
                        builtEmail,
                            EmailUtils.smtpClient,
                            true
                    ).Wait(); // Wait for the task to complete
                }
                catch (Exception e)
                {
                    // Log the exception, handle it, or notify an administrator
                    
                    Console.WriteLine(e);
                }
            });
        }
        catch (Exception e)
        {
            Console.WriteLine(e);
        }
        return new OperationResult
        {
            IsSuccess = true,
            Message = $"Successfully sent an email!"
        };
    }
}