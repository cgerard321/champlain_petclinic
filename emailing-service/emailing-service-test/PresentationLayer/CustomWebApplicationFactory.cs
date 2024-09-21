using System;
using emailing_service.Models;
using emailing_service.Utils;
using Microsoft.AspNetCore.Mvc.Testing;

namespace emailing_service_test.PresentationLayer;

internal class CustomWebApplicationFactory : WebApplicationFactory<Program>
{
    protected override void ConfigureWebHost(Microsoft.AspNetCore.Hosting.IWebHostBuilder builder)
    {
        // Load environment variables

        builder.ConfigureServices(services =>
        {
            // Load SMTP settings from environment variables
            try
            {
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
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Error loading SMTP settings: {ex.Message}");
            }

            // Setup the email utility singleton
            EmailUtils.SetUpEmailUtils();
        });
    }
}