using DotNetEnv;
using emailing_system.BackgroundTask;
using emailing_system.Controllers;
using emailing_system.Models;
using emailing_system.Utils;

var builder = WebApplication.CreateBuilder(args);

// Add environment variable loading

Env.Load(Directory.GetCurrentDirectory()+"mailer.env");

// Log the current directory (for debugging)
// Log the current directory (for debugging)
Console.WriteLine($"Path of docker is : {Directory.GetCurrentDirectory()}");

// Load SMTP settings from environment variables
try
{
    // Check for null or empty values
    var smtpServer = Env.GetString("SMTP_SERVER") ?? throw new ArgumentNullException("SMTP_SERVER");
    var smtpPort = Env.GetInt("SMTP_PORT");
    var smtpUsername = Env.GetString("SMTP_USERNAME") ?? throw new ArgumentNullException("SMTP_USERNAME");
    var smtpPassword = Env.GetString("SMTP_PASSWORD") ?? throw new ArgumentNullException("SMTP_PASSWORD");
    var smtpEmail = Env.GetString("SMTP_EMAIL") ?? throw new ArgumentNullException("SMTP_EMAIL");
    var smtpDisplayName = Env.GetString("SMTP_DISPLAY_NAME") ?? throw new ArgumentNullException("SMTP_DISPLAY_NAME");

    
    EmailUtils.emailConnectionString = new ConnectionEmailServer(
        smtpServer,
        smtpPort,
        smtpUsername,
        smtpPassword,
        smtpEmail,
        smtpDisplayName
    );
    Console.WriteLine($"SMTP server is : {EmailUtils.emailConnectionString.ToString()}");
}
catch (Exception ex)
{
    Console.WriteLine($"Error loading SMTP settings: {ex.Message}");
}


// Get the path for the default HTML file from configuration
string pathOfDefaultHtml = builder.Configuration["HtmlFilePath"];

if (!string.IsNullOrEmpty(pathOfDefaultHtml))
{
    EmailController.EmailTemplates.Add(new EmailTemplate("Default", File.ReadAllText(pathOfDefaultHtml)));
}

// Add services to the container
builder.Services.AddControllersWithViews();
builder.Services.AddHostedService<RecurringJobService>();

var app = builder.Build();

// Configure the HTTP request pipeline
if (!app.Environment.IsDevelopment())
{
    app.UseExceptionHandler("/Home/Error");
}

app.UseStaticFiles();
app.UseRouting();
app.UseAuthorization();

app.MapControllerRoute(
    name: "default",
    pattern: "{controller=Home}/{action=Index}/{id?}");


// Ensure the application listens on all network interfaces
app.Urls.Add("http://0.0.0.0:5115");

app.Run();