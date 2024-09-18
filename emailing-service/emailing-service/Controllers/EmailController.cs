using emailing_service.Models;
using emailing_service.Models.EmailType;
using emailing_service.Utils;
using emailing_service.Utils.Exception;
using Microsoft.AspNetCore.Mvc;

namespace emailing_service.Controllers;

//THIS MEANS THAT THE ROUTE STARTS WITH     VVV
//                                    api/controller


[Route("email")]
[ApiController]
public class EmailController : Controller
{
    
    [HttpGet("test")]
    public IActionResult TestEndpoint()
    {
        Console.WriteLine("TestEndpoint accessed");
        return Ok(); // Returns a 200 OK status with no content
    }


    
    [HttpPost("templates/add/{templateName}")]
    [Consumes("text/html")] 
    // Specify that this endpoint accepts HTML content
    public async Task<IActionResult> ReceiveHtml(string templateName)
    {
        using (StreamReader reader = new StreamReader(Request.Body))
        {
            string htmlContent = await reader.ReadToEndAsync();
            
            if (string.IsNullOrWhiteSpace(templateName))
                return BadRequest($"Template Name is required. [{templateName}] is not valid as a template name.");
            if (string.IsNullOrWhiteSpace(htmlContent))
                return BadRequest("HTML content was missing"); 
            if (EmailUtils.EmailTemplates.FirstOrDefault(e => e.Name == templateName) != null)
                return BadRequest("Template already exists");
            EmailUtils.EmailTemplates.Add(
                new EmailTemplate(
                    templateName, 
                    htmlContent
                    )
                );
            return Ok($"HTML content for template '{templateName}' received successfully!");
        }
    }
    
    [HttpPost("send")]
    public IActionResult Post([FromBody] DirectEmailModel emailModel)
    {
        Console.WriteLine("Received Email Call Function!");
        if (emailModel == null)
            return BadRequest("Email Model is null.");
        DirectEmailModel directEmailModel = emailModel;
        Console.WriteLine("Found the model!" + directEmailModel.ToString());
        
        
        if (directEmailModel.EmailToSendTo == null)
            return BadRequest("Email To Send To is null. EMAIL IS REQUIRED");
        if(!EmailUtils.CheckIfEmailIsValid(directEmailModel.EmailToSendTo))
            return BadRequest("Email To Send To Not Valid");
        if(directEmailModel.EmailTitle == null)
            return BadRequest("Email Title is null.");
        if (directEmailModel.TemplateName == null)
            directEmailModel.TemplateName = "Default";
        EmailTemplate? emailTemplate = EmailUtils.EmailTemplates.FirstOrDefault(e => e.Name == directEmailModel.TemplateName);
        if (emailTemplate==null)
            return BadRequest("Template does not exist. Please create a template first or use the default one (Default)");
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
            return BadRequest("Email Template is empty. Please create a template first or use the default one (Default)");
        }
        catch (EmailStringContainsPlaceholder e)
        {
            Console.WriteLine(e);
            return BadRequest(e);
        }
        catch (TemplateRequiredFieldNotSet e)
        {
            Console.WriteLine(e);
            return BadRequest(e);
        }
        catch (TriedToFillEmailFieldWithEmptyWhiteSpace e)
        {
            Console.WriteLine(e);
            return BadRequest(e);
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
            return StatusCode(500, "Failed to start email sending process.");
        }
        // Logic to save the email recipient to a database
        return Ok($"Received email recipient for");
    }
}
