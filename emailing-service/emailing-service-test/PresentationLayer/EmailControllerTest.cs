using System.Net;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;
using emailing_service.Models.EmailType;
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
    private CustomWebApplicationFactory _factory;
    private HttpClient _httpClient;

    [SetUp]
    public void Setup()
    {
        // Initialize the custom factory and create the HttpClient
        _factory = new CustomWebApplicationFactory();
        _httpClient = _factory.CreateClient(); // Use CreateClient instead of Create
    }

    [Test]
    public async Task TestEndpoint_ReturnsOk()
    {
        // Arrange
        var request = new HttpRequestMessage(HttpMethod.Get, "email/test");

        // Act
        var response = await _httpClient.SendAsync(request);

        // Assert
        Assert.AreEqual(HttpStatusCode.OK, response.StatusCode);
    }

    [Test]
    public async Task ReceiveHtml_TemplateNameIsValid_ReturnsOk()
    {
        // Arrange
        var templateName = "valid-template";
        var htmlContent = "<html><body>Hello World!</body></html>";
        var request = new HttpRequestMessage(HttpMethod.Post, $"email/templates/add/{templateName}")
        {
            Content = new StringContent(htmlContent, Encoding.UTF8, "text/html")
        };

        // Act
        var response = await _httpClient.SendAsync(request);

        // Assert
        Assert.AreEqual(HttpStatusCode.OK, response.StatusCode);
    }

    [TearDown]
    public void TearDown()
    {
        _httpClient.Dispose();
        _factory.Dispose();
    }
}
