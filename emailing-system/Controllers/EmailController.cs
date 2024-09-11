using emailing_system.Models;
using Microsoft.AspNetCore.Mvc;

namespace emailing_system.Controllers;

[Route("api/[controller]")]
[ApiController]
public class EmailController : Controller
{
    [HttpPost]
    public IActionResult Post([FromBody] EmailRecipient recipient)
    {
        if (recipient == null)
        {
            return BadRequest("Email recipient is null.");
        }

        // You can add logic here to save the email recipient to a database

        return Ok($"Received email recipient for {recipient.Name} {recipient.LastName}");
    }
    
}