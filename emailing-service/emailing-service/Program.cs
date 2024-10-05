using System.Runtime.CompilerServices;
using DotNetEnv;
using emailing_service.BackgroundTask;
using emailing_service.BuisnessLayer;
//using emailing_service.BackgroundTask;
using emailing_service.Models;
using emailing_service.Models.Database;
using emailing_service.Utils;

[assembly: InternalsVisibleTo("emailing_service_test")] // Adjust the namespace if needed
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
    var smtpServer = builder.Configuration["SMTP:SMTP_SERVER"];//Env.GetString("SMTP_SERVER") ?? throw new ArgumentNullException("SMTP_SERVER");
    var smtpPort = builder.Configuration.GetValue<int>("SMTP:SMTP_PORT");//Env.GetInt("SMTP_PORT");
    var smtpUsername = builder.Configuration["SMTP:SMTP_USERNAME"];;//Env.GetString("SMTP_USERNAME") ?? throw new ArgumentNullException("SMTP_USERNAME");
    var smtpEmail = builder.Configuration["SMTP:SMTP_EMAIL"];;//Env.GetString("SMTP_EMAIL") ?? throw new ArgumentNullException("SMTP_EMAIL");
    var smtpDisplayName = builder.Configuration["SMTP:SMTP_DISPLAY_NAME"];//Env.GetString("SMTP_DISPLAY_NAME") ?? throw new ArgumentNullException("SMTP_DISPLAY_NAME");
    //We are only getting this from the .env file
    var smtpPassword = Env.GetString("SMTP_PASS") ?? throw new ArgumentNullException("SMTP_PASS");


    EmailUtils.emailConnectionString = new ConnectionEmailServer(
        smtpServer,
        smtpPort,
        smtpUsername,
        smtpPassword,
        smtpEmail,
        smtpDisplayName
    );
}
catch (Exception ex)
{
    Console.WriteLine($"Error loading SMTP settings: {ex.Message}. Now Entering PlaceholderData");
    EmailUtils.emailConnectionString = new ConnectionEmailServer(
        "smtpServer",
        123,
        "helloWorld",
        "smtpPassword",
        "smtpEmail",
        "ChamplainPetClinic"
    );
}
//Just initialising the singleton pattern 
EmailUtils.SetUpEmailUtils();

// Get the path for the default HTML file from configuration
string? pathOfDefaultHtml = builder.Configuration["HtmlFilePath"];
try
{
    if (!string.IsNullOrEmpty(pathOfDefaultHtml))
    {
        EmailUtils.EmailTemplates.Add(new EmailTemplate("Default", File.ReadAllText(pathOfDefaultHtml)));
    }
}
catch (DirectoryNotFoundException e)
{
    Console.WriteLine(e);
    Console.WriteLine("Could not load HTML file. This means that we do not have the default template loaded. IGNORE THIS IF IN TEST");
}


// Configure database connection string
String connectionString;
try
{
    connectionString = builder.Configuration["DEFAULT_CONNECTION"];
}
catch (ArgumentNullException)
{
    //We just make sure the DB string is valid for testing!
    Console.WriteLine("We did not find the string connection, as such, we will use a placeholder one!");
    connectionString = "Server=emailing-service-mysql-db;Port=3308;Database=MyDatabaseExample;User=root;Password=Example;";
}
DatabaseHelper._connectionString = connectionString;
builder.Services.AddTransient<IDatabaseHelper, DatabaseHelper>();
IDatabaseHelper dbHelper = new DatabaseHelper();
Console.WriteLine("we reached the tablecreation");
//May there be an error here check later!
dbHelper.CreateTableAsync(50);
//builder.Services.AddScoped<IEmailService, EmailServiceImpl>();


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



