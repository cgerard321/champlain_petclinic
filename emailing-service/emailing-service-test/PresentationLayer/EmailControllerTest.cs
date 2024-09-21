using System;
using System.Net;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;
using emailing_service.Models;
using emailing_service.Models.EmailType;
using emailing_service.Utils;
using Microsoft.AspNetCore;
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.AspNetCore.TestHost;
using Microsoft.VisualStudio.TestPlatform.TestHost;
using Newtonsoft.Json;
using NUnit.Framework;

namespace emailing_service_test.PresentationLayer;
[TestFixture]
public class EmailControllerTests
{
    private readonly HttpClient _httpClient;
    private readonly string pathOfDefaultHtml;
    private readonly DirectEmailModel directEmailModel;
    public EmailControllerTests()
    {
        var applicationFactory = new WebApplicationFactory<Program>();
        _httpClient = applicationFactory.CreateClient();
        pathOfDefaultHtml = "<html><body>%%EMAIL_HEADERS%% %%EMAIL_BODY%% %%EMAIL_FOOTER%% %%EMAIL_NAME%% %%EMAIL_SENDER%%</body></html>";
        directEmailModel = new DirectEmailModel(
            "xilef992@gmail.com",
            "This is a test email",
            "Default",
            "This is the emailHeader",
            "This is the emailbody",
            "this is the email footer",
            "Felix",
            "PetClinic"
        );
    }
    [Test]
    public async Task TestEndpoint_ReturnsOkResult()
    {
        // Act
        var response = await _httpClient.GetAsync("email/test");

        // Assert
        Assert.AreEqual(HttpStatusCode.OK, response.StatusCode);
    }
    [Test]
    [TestCase("Template1")]
    [TestCase("Template2")]
    [TestCase("Template3")]
    public async Task ReceiveHtml_TemplateNameIsValid_ReturnsOkResult(string name)
    {

        // Act
        var request = new HttpRequestMessage(HttpMethod.Post, $"email/templates/add/{name}")
        {
            Content = new StringContent(pathOfDefaultHtml, Encoding.UTF8, "text/html")
        };
        var response = await _httpClient.SendAsync(request);

        // Assert
        Assert.AreEqual(HttpStatusCode.OK, response.StatusCode);
    }

    [Test]
    [TestCase(null)]
    [TestCase("")]
    [TestCase(" ")]
    public async Task ReceiveHtml_TemplateNameIsNullOrWhiteSpace_ReturnsBadRequest(string? templateName)
    {
        EmailUtils.EmailTemplates.Clear();
        var json = JsonConvert.SerializeObject(directEmailModel);
        // Act
        var request = new HttpRequestMessage(HttpMethod.Post, $"email/templates/add/{templateName}")
        {
            Content = new StringContent(json, Encoding.UTF8, "text/html")
        };
        var response = await _httpClient.SendAsync(request);

        //NotFound because you can't send a Null or whitspace as a parameter to an api call
        Assert.AreEqual(HttpStatusCode.NotFound, response.StatusCode);
    }
    [Test]
    public async Task SendEmail_EmailModelIsValid_ReturnsOkResult()
    {
        EmailUtils.EmailTemplates.Add(new EmailTemplate("Default","<html><body>%%EMAIL_HEADERS%% %%EMAIL_BODY%% %%EMAIL_FOOTER%% %%EMAIL_NAME%% %%EMAIL_SENDER%%</body></html>"));
        // Arrange
        var emailModel = directEmailModel;
        var json = JsonConvert.SerializeObject(emailModel);

        // Act
        var request = new HttpRequestMessage(HttpMethod.Post, "email/send")
        {
            Content = new StringContent(json, Encoding.UTF8, "application/json")
        };
        var response = await _httpClient.SendAsync(request);

        // Assert
        Assert.AreEqual(HttpStatusCode.OK, response.StatusCode);
    }

    [Test]
    public async Task SendEmail_EmailModelIsNull_ReturnsBadRequest()
    {
        // Arrange
        var requestBody = "{}"; // or serialize an EmailModel object to JSON
        var request = new HttpRequestMessage(HttpMethod.Post, "email/send")
        {
            Content = new StringContent(requestBody, Encoding.UTF8, "application/json")
        };

        // Act
        var response = await _httpClient.SendAsync(request);

        // Assert
        Assert.AreEqual(HttpStatusCode.BadRequest, response.StatusCode);
    }

    [OneTimeTearDown]
    public void TearDown()
    {
        _httpClient.Dispose();
    }
    // Test methods go here
}