using System;
using System.IO;
using DotNetEnv;
using emailing_service.BackgroundTask;
using emailing_service.Models;
using emailing_service.Utils;
using Microsoft.AspNetCore.Builder;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;

namespace emailing_service_test.PresentationLayer;

public class WebApplicationFactory
{
    public WebApplication Create()
    {
        var builder = WebApplication.CreateBuilder();
        // Add environment variable loading
        Console.WriteLine(Directory.GetCurrentDirectory());

        // Log the current directory (for debugging)
        Console.WriteLine($"Path of docker is : {Directory.GetCurrentDirectory()}");

        // Load SMTP settings from environment variables
        try
        {
            // Check for null or empty values
            var smtpServer = "smtp.gmail.com";
            var smtpPort = 510;
            var smtpUsername = "ExampleUsername";
            var smtpPassword = "1wadasdasdasdasd";
            var smtpEmail = "xilef992@gmail.com";
            var smtpDisplayName = "PetClinic";

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
        //Just initialising the singleton pattern 
        EmailUtils.SetUpEmailUtils();

        // Get the path for the default HTML file from configuration
        string? pathOfDefaultHtml = builder.Configuration["HtmlFilePath"];

        /*try
        {
            if (!string.IsNullOrEmpty(pathOfDefaultHtml))
            {
                EmailUtils.EmailTemplates.Add(new EmailTemplate("Default", File.ReadAllText(pathOfDefaultHtml)));
            }
        }
        catch (DirectoryNotFoundException e)
        {
            Console.WriteLine(e);
            Console.WriteLine("Could not load HTML file. This means that we do not have the default template loaded");
            throw;
        }*/

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

        return app;
    }
}