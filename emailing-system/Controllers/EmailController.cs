using emailing_system.Models;
using Microsoft.AspNetCore.Mvc;

namespace emailing_system.Controllers;

//THIS MEANS THAT THE ROUTE STARTS WITH     VVV
//                                    api/controller


[Route("[controller]")]
[ApiController]
public class EmailController : Controller
{
    //TODO create default template
    public static List<EmailTemplate> EmailTemplates = new List<EmailTemplate>();
    
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
            if (EmailTemplates.FirstOrDefault(e => e.Name == templateName) != null)
                return BadRequest("Template already exists");
            EmailTemplates.Add(
                new EmailTemplate(
                    templateName, 
                    htmlContent
                    )
                );
            return Ok($"HTML content for template '{templateName}' received successfully!");
        }
    }
    
    [HttpPost("send")]
    public IActionResult Post([FromBody] EmailRecipient recipient)
    {
        if (recipient == null)
        {
            return BadRequest("Email recipient is null.");
        }

        // Logic to save the email recipient to a database

        return Ok($"Received email recipient for {recipient.Name} {recipient.LastName}");
    }
}
