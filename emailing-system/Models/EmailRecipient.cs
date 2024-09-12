using System.ComponentModel.DataAnnotations;
using System.Runtime.InteropServices.JavaScript;

namespace emailing_system.Models;

public class EmailRecipient
{
    [Key]
    public int Id { get; set; }
    public bool ShouldSendReminder { get; set; }
    public DateTime DateOfReminder { get; set; }

    public string EmailAddress { get; set; }
    public string Name { get; set; }
    public string LastName { get; set; }
    public string DateToSend { get; set; }
    public string EmailTitle { get; set; }
    public string EmailHeader { get; set; }
    public string EmailBody { get; set; }
    public string EmailFooter { get; set; }
}