using System.Diagnostics.CodeAnalysis;
using emailing_service.BuisnessLayer;
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
    //I am not the one calling the constructor. I have to do it like this
    private readonly IEmailService _emailService = new EmailServiceImpl();
    
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
        try
        {
            using (StreamReader reader = new StreamReader(Request.Body))
            {

                var result = _emailService.ReceiveHtml(templateName, await reader.ReadToEndAsync());
            }
        }
        catch (CreatedAlreadyExistingTemplate e)
        {
            Console.WriteLine(e);
            return BadRequest(new { message = e.Message });
        }
        catch (TemplateFormatException e)
        {
            Console.WriteLine(e);
            return BadRequest(new { message = e.Message });
        }
        return Ok();
    }
    
    [HttpPost("send")]
    public IActionResult SendEmail([FromBody] DirectEmailModel emailModel)
    {
        if(emailModel ==null || emailModel.IsEmpty())
        {
            return NoContent();
        }
        try
        {
            var result = _emailService.SendEmail(emailModel);
        }
        catch (MissingBodyException e)
        {
            Console.WriteLine(e);
            return NoContent();
        }
        catch (BadEmailModel e)
        {
            Console.WriteLine(e);
            return BadRequest(new { message = e.Message });
        }
        catch (TriedToFindNonExistingTemplate e)
        {
            Console.WriteLine(e);
            return NotFound(new { message = e.Message });
        }
        catch (NullReferenceException e)
        {
            Console.WriteLine(e);
            return NotFound(new { message = e.Message });
        }
        catch (EmailStringContainsPlaceholder e)
        {
            Console.WriteLine(e);
            return BadRequest(new { message = e.Message });
        }
        catch (TemplateRequiredFieldNotSet e)
        {
            Console.WriteLine(e);
            return BadRequest(new { message = e.Message });
        }
        catch (Exception e)
        {
            Console.WriteLine(e);
            return BadRequest(new { message = e.Message });
        }
        return Ok();
    }
}
