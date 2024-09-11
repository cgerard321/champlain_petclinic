using emailing_system.Models;
using Microsoft.AspNetCore.Mvc;

namespace emailing_system.Controllers;

//THIS MEANS THAT THE ROUTE STARTS WITH     VVV
//                                    api/controller


[Route("api/[controller]")]
[ApiController]
public class EmailController : Controller
{
    [HttpPost("recipients/send")]
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
