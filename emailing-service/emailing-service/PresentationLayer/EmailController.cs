using emailing_service.BuisnessLayer;
using emailing_service.Models.Database;
using emailing_service.Models.EmailType;
using emailing_service.Utils.Exception;
using Microsoft.AspNetCore.Mvc;

namespace emailing_service.PresentationLayer;

//THIS MEANS THAT THE ROUTE STARTS WITH     VVV
//                                    api/controller


[Route("email")]
[ApiController]
public class EmailController : Controller
{
    //I am not the one calling the constructor. I have to do it like this
    private IEmailService _emailService = new EmailServiceImpl();
    

    [HttpGet("test")]
    public IActionResult TestEndpoint()
    {
        Console.WriteLine("TestEndpoint accessed");
        return Ok(); // Returns a 200 OK status with no content
    }
    

    [HttpGet("get")]
    public IActionResult GetAllEmails()
    {
        List<EmailModel> emails;
        try
        {
            emails = _emailService.GetAllEmails();
        }
        catch (MissingDatabaseException)
        {
            return StatusCode(503, "Database failure! Make sure you are running MySql Server.");
        }
         // Replace `_emailService` with your service
        return Ok(emails); // This returns the list as JSON
    }

    [HttpGet("send/notification")]
    public IActionResult GetAllNotificationEmails()
    {
        List<EmailModelNotification> emails;
        try
        {
            emails = _emailService.GetAllNotificationEmails();
        }
        catch (MissingDatabaseException)
        {
            return StatusCode(503, "Database failure! Make sure you are running MySql Server.");
        }
        // Replace `_emailService` with your service
        return Ok(emails); // This returns the list as JSON
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
                string? templateContent = await reader.ReadToEndAsync();
                if (String.IsNullOrWhiteSpace(templateContent))
                {
                    return NoContent();
                }
                _emailService.ReceiveHtml(templateName, templateContent);
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
        return Ok($"Successfully created a new template with the name {templateName}");
    }
    
    [HttpPost("send")]
    public IActionResult SendEmail([FromBody] DirectEmailModel? emailModel)
    {
        if( emailModel ==null || emailModel.IsEmpty())
        {
            return NoContent();
        }
        try
        {
            _emailService.SendEmail(emailModel);
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
        /*catch (NullReferenceException e)
        {
            Console.WriteLine(e);
            return NotFound(new { message = e.Message });
        }*/
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
        return Ok();
    }
    [HttpPost("send/notification")]
    public IActionResult SendEmailNotification([FromBody] NotificationEmailModel? emailModel)
    {
        if( emailModel ==null || emailModel.IsEmpty())
        {
            return NoContent();
        }

        try
        {
            _emailService.SendEmailNotification(emailModel);
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
        catch(AlreadyPassedDate e)
        {
            Console.WriteLine(e);
            return BadRequest(new { message = e.Message });
        }
        /*catch (NullReferenceException e)
        {
            Console.WriteLine(e);
            return NotFound(new { message = e.Message });
        }*/
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
        return Ok();
    }
    
    [HttpPost("send/raw")]
    public IActionResult SendEmailRaw([FromBody] RawEmailModel? emailModel)
    {
        if( emailModel ==null || emailModel.IsEmpty())
        {
            return NoContent();
        }

        try
        {
            _emailService.SendRawEmail(emailModel);
        }
        catch (BadEmailModel e)
        {
            Console.WriteLine(e);
            return BadRequest(new { message = e.Message });
        }
        return Ok();
    }

    /*
    [HttpPost("send-reminder-email")]
    public IActionResult SendReminderEmail([FromBody] ReminderEmailModel model)
    {
        if (model == null)
        {
            return BadRequest("Model is null");
        }

        try
        {
            var result = _emailService.SendReminderEmail(model);
            if (result.IsSuccess)
            {
                return Ok(result.Message);
            }
            else
            {
                return StatusCode(500, result.Message);
            }
        }
        catch (BadEmailModel ex)
        {
            return BadRequest(ex.Message);
        }
        catch (InvalidOperationException ex)
        {
            return BadRequest(ex.Message);
        }
        catch (Exception ex)
        {
            return StatusCode(500, $"Internal server error: {ex.Message}");
        }
    }
    
}*/
}
