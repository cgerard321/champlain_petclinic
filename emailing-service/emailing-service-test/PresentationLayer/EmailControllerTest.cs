using System.Net;
using System.Text;
using emailing_service.Models;
using emailing_service.Models.EmailType;
using emailing_service.Utils;
using Microsoft.AspNetCore.Mvc.Testing;
using Newtonsoft.Json;


namespace emailing_service_test.PresentationLayer;
[TestFixture]
public class EmailControllerTests
{
    private readonly HttpClient _httpClient;
    private readonly string _pathOfDefaultHtml;
    private readonly DirectEmailModel _directEmailModel;
    public EmailControllerTests()
    {
        EmailUtils.sendEmail = false;
        var applicationFactory = new WebApplicationFactory<Program>();
        _httpClient = applicationFactory.CreateClient();
        _pathOfDefaultHtml = "<html><body>%%EMAIL_HEADERS%% %%EMAIL_BODY%% %%EMAIL_FOOTER%% %%EMAIL_NAME%% %%EMAIL_SENDER%%</body></html>";
        _directEmailModel = new DirectEmailModel(
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
    [SetUp]
    public void Setup()
    {
        EmailUtils.EmailTemplates.Clear();
    }
    [Test]
    public async Task TestEndpoint_ReturnsOkResult()
    {
        // Act
        var response = await _httpClient.GetAsync("email/test");

        // Assert
        Assert.That(response.StatusCode, Is.EqualTo(HttpStatusCode.OK));
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
            Content = new StringContent(_pathOfDefaultHtml, Encoding.UTF8, "text/html")
        };
        var response = await _httpClient.SendAsync(request);

        // Assert
        Assert.That(response.StatusCode, Is.EqualTo(HttpStatusCode.OK));
    }

    [Test]
    [TestCase(null)]
    [TestCase("")]
    [TestCase(" ")]
    public async Task ReceiveHtml_TemplateNameIsNullOrWhiteSpace_ReturnsBadRequest(string? templateName)
    {
        var json = JsonConvert.SerializeObject(_directEmailModel);
        // Act
        var request = new HttpRequestMessage(HttpMethod.Post, $"email/templates/add/{templateName}")
        {
            Content = new StringContent(json, Encoding.UTF8, "text/html")
        };
        var response = await _httpClient.SendAsync(request);

        //NotFound because you can't send a Null or whitspace as a parameter to an api call
        Assert.That(response.StatusCode, Is.EqualTo(HttpStatusCode.NotFound));
    }
    
    [Test]
    [TestCase("Default")]
    [TestCase("Example2")]
    [TestCase("ItCanHaveAnyNameReally...")]
    public async Task ReceiveHtml_TemplateNameAlreadyExist_ReturnsBadRequest(string templateName)
    {
        
        EmailUtils.EmailTemplates.Add(new EmailTemplate(templateName,"<html><body>%%EMAIL_HEADERS%% %%EMAIL_BODY%% %%EMAIL_FOOTER%% %%EMAIL_NAME%% %%EMAIL_SENDER%%</body></html>"));
        // Act
        var request = new HttpRequestMessage(HttpMethod.Post, $"email/templates/add/{templateName}")
        {
            Content = new StringContent(_pathOfDefaultHtml, Encoding.UTF8, "text/html")
        };
        var response = await _httpClient.SendAsync(request);

        //NotFound because you can't send a Null or whitspace as a parameter to an api call
        Assert.That(response.StatusCode, Is.EqualTo(HttpStatusCode.BadRequest));
    }
    [Test]
    [TestCase("")]
    public async Task ReceiveHtml_TemplateBodyIsEmpty_ReturnNoContent(string template)
    {
        // Act
        var request = new HttpRequestMessage(HttpMethod.Post, $"email/templates/add/Example")
        {
            Content = new StringContent(template, Encoding.UTF8, "text/html")
        };
        var response = await _httpClient.SendAsync(request);

        // Assert
        Assert.That(response.StatusCode, Is.EqualTo(HttpStatusCode.NoContent));
    }
    [Test]
    public async Task SendEmail_EmailModelIsValid_ReturnsOkResult()
    {
        EmailUtils.EmailTemplates.Add(new EmailTemplate("Default","<html><body>%%EMAIL_HEADERS%% %%EMAIL_BODY%% %%EMAIL_FOOTER%% %%EMAIL_NAME%% %%EMAIL_SENDER%%</body></html>"));
        // Arrange
        var emailModel = _directEmailModel;
        var json = JsonConvert.SerializeObject(emailModel);

        // Act
        var request = new HttpRequestMessage(HttpMethod.Post, "email/send")
        {
            Content = new StringContent(json, Encoding.UTF8, "application/json")
        };
        var response = await _httpClient.SendAsync(request);

        // Assert
        Assert.That(response.StatusCode, Is.EqualTo(HttpStatusCode.OK));
    }

    [Test]
    public async Task SendEmail_EmailModelIsNull_ReturnsBadRequest()
    {
        // Arrange
        var requestBody = "{}"; 
        var request = new HttpRequestMessage(HttpMethod.Post, "email/send")
        {
            Content = new StringContent(requestBody, Encoding.UTF8, "application/json")
        };

        // Act
        var response = await _httpClient.SendAsync(request);

        // Assert
        Assert.That(response.StatusCode, Is.EqualTo(HttpStatusCode.BadRequest));
    }

    //let's have fun and test all the exceptions!
    [Test]
    public async Task SendEmail_EmailModelHasMissingBody_BadRequest()
    {
        EmailUtils.EmailTemplates.Add(new EmailTemplate("Default","<html><body>%%EMAIL_HEADERS%% %%EMAIL_BODY%% %%EMAIL_FOOTER%% %%EMAIL_NAME%% %%EMAIL_SENDER%%</body></html>"));

        // Arrange
        var emailModel = new DirectEmailModel(); // create an empty DirectEmailModel instance
        var json = JsonConvert.SerializeObject(emailModel);

        // Act
        var request = new HttpRequestMessage(HttpMethod.Post, "email/send")
        {
            Content = new StringContent(json, Encoding.UTF8, "application/json")
        };
        var response = await _httpClient.SendAsync(request);

        // Assert
        Assert.That(response.StatusCode, Is.EqualTo(HttpStatusCode.BadRequest));
    }
    public static IEnumerable<DirectEmailModel> InvalidBodyRequest()
    {
        yield return new DirectEmailModel(
            "",
            "This is a test email",
            "Default",
            "This is the emailHeader",
            "This is the email body",
            "this is the email footer",
            "Felix",
            "PetClinic"
        );

        yield return new DirectEmailModel(
            " ",
            "Another test email",
            "Default",
            "Test email header",
            "Test email body",
            "Test email footer",
            "John",
            "CompanyXYZ"
        );
        yield return new DirectEmailModel(
            null!,
            "Another test email",
            "Default",
            "Test email header",
            "Test email body",
            "Test email footer",
            "John",
            "CompanyXYZ"
        );
        yield return new DirectEmailModel(
            "exam@ple2@test.com",
            "Another test email",
            "Default",
            "Test email header",
            "Test email body",
            "Test email footer",
            "John",
            "CompanyXYZ"
        );
        yield return new DirectEmailModel(
            "@test.com",
            "Another test email",
            "Default",
            "Test email header",
            "Test email body",
            "Test email footer",
            "John",
            "CompanyXYZ"
        );
        yield return new DirectEmailModel(
            "example2@test",
            "Another test email",
            "Default",
            "Test email header",
            "Test email body",
            "Test email footer",
            "John",
            "CompanyXYZ"
        );
        yield return new DirectEmailModel(
            "example2@tes@t.com",
            "Another test email",
            "Default",
            "Test email header",
            "Test email body",
            "Test email footer",
            "John",
            "CompanyXYZ"
        );
        yield return new DirectEmailModel(
            "example2@test.com",
            "",
            "Default",
            "Test email header",
            "Test email body",
            "Test email footer",
            "John",
            "CompanyXYZ"
        );
        yield return new DirectEmailModel(
            "example2@test.com",
            "    ",
            "Default",
            "Test email header",
            "Test email body",
            "Test email footer",
            "John",
            "CompanyXYZ"
        );
        yield return new DirectEmailModel(
            "example2@test.com",
            null!,
            "Default",
            "Test email header",
            "Test email body",
            "Test email footer",
            "John",
            "CompanyXYZ"
        );
    }
    [Test, TestCaseSource(nameof(InvalidBodyRequest))]
    public async Task SendEmail_EmailBodyIsInvalid_BadRequest(DirectEmailModel emailModel)
    {
        EmailUtils.EmailTemplates.Add(new EmailTemplate("Default","<html><body>%%EMAIL_HEADERS%% %%EMAIL_BODY%% %%EMAIL_FOOTER%% %%EMAIL_NAME%% %%EMAIL_SENDER%%</body></html>"));
        
        var json = JsonConvert.SerializeObject(emailModel);
        // Act
        var request = new HttpRequestMessage(HttpMethod.Post, "email/send")
        {
            Content = new StringContent(json, Encoding.UTF8, "application/json")
        };
        var response = await _httpClient.SendAsync(request);

        // Assert
        Assert.That(response.StatusCode, Is.EqualTo(HttpStatusCode.BadRequest));
    }
    public static IEnumerable<DirectEmailModel> ValidBodyWithNonExistingTemplate()
    {
        yield return new DirectEmailModel(
            "example@test.com",
            "This is a test email",
            "Example1",
            "This is the emailHeader",
            "This is the emailbody",
            "this is the email footer",
            "Felix",
            "PetClinic"
        );

        yield return new DirectEmailModel(
            "example@test.com",
            "Another test email",
            "Example2",
            "Test email header",
            "Test email body",
            "Test email footer",
            "John",
            "CompanyXYZ"
        );
        yield return new DirectEmailModel(
            "example@test.com",
            "Another test email",
            "Example3",
            "Test email header",
            "Test email body",
            "Test email footer",
            "John",
            "CompanyXYZ"
        );
    }
    [Test, TestCaseSource(nameof(ValidBodyWithNonExistingTemplate))]
    public async Task SendEmail__BadRequest(DirectEmailModel emailModel)
    {
        //EmailUtils.EmailTemplates.Add(new EmailTemplate("Default","<html><body>%%EMAIL_HEADERS%% %%EMAIL_BODY%% %%EMAIL_FOOTER%% %%EMAIL_NAME%% %%EMAIL_SENDER%%</body></html>"));
        
        var json = JsonConvert.SerializeObject(emailModel);
        // Act
        var request = new HttpRequestMessage(HttpMethod.Post, "email/send")
        {
            Content = new StringContent(json, Encoding.UTF8, "application/json")
        };
        var response = await _httpClient.SendAsync(request);

        // Assert
        Assert.That(response.StatusCode, Is.EqualTo(HttpStatusCode.NotFound));
    }
    
    public static IEnumerable<DirectEmailModel> EmptyNeededFieldsModels()
    {
        yield return new DirectEmailModel(
            "example@test.com",
            "This is a test email",
            "Default",
            null!,
            "This is the emailbody",
            "this is the email footer",
            "Felix",
            "PetClinic"
        );

        yield return new DirectEmailModel(
            "example@test.com",
            "Another test email",
            "Default",
            "Test email header",
            null!,
            "Test email footer",
            "John",
            "CompanyXYZ"
        );
        yield return new DirectEmailModel(
            "example@test.com",
            "Another test email",
            "Default",
            "Test email header",
            "Test email body",
            null!,
            "John",
            "CompanyXYZ"
        );
        yield return new DirectEmailModel(
            "example@test.com",
            "This is a test email",
            "Default",
            "This is the emailHeader",
            "This is the emailbody",
            "this is the email footer",
            null!,
            "PetClinic"
        );

        yield return new DirectEmailModel(
            "example@test.com",
            "Another test email",
            "Default",
            "Test email header",
            "Test email body",
            "Test email footer",
            "John",
            null!
        );
    }
    [Test, TestCaseSource(nameof(EmptyNeededFieldsModels))]
    public async Task SendEmail_NotAllTheFieldsOfTheTemmplate_ReturnsBadRequest(DirectEmailModel emailModelParam)
    {
        EmailUtils.EmailTemplates.Add(new EmailTemplate("Default","<html><body>%%EMAIL_HEADERS%% %%EMAIL_BODY%% %%EMAIL_FOOTER%% %%EMAIL_NAME%% %%EMAIL_SENDER%%</body></html>"));
        // Arrange
        //var httpClient = new HttpClient();
        var emailModel = emailModelParam;
        emailModel.TemplateName = "Default";
        var json = JsonConvert.SerializeObject(emailModel);

        var request = new HttpRequestMessage(HttpMethod.Post, "email/send")
        {
            Content = new StringContent(json, Encoding.UTF8, "application/json")
        };
        var response = await _httpClient.SendAsync(request);
        // Act
        // Assert
        Assert.That(response.StatusCode, Is.EqualTo(HttpStatusCode.BadRequest));
    }
    [Test]
    public async Task GetAllEmails_SqlFailure_ReturnStatusCode503()
    {
        var request = new HttpRequestMessage(HttpMethod.Get, "email/get");
        var response = await _httpClient.SendAsync(request);

        Console.WriteLine(response.Content.ReadAsStringAsync().Result);
        // Act
        // Assert
        Assert.That(response.StatusCode, Is.EqualTo(HttpStatusCode.ServiceUnavailable));
    }
    
    /*public static IEnumerable<DirectEmailModel> FieldContainsPlaceHolder()
    {
        yield return new DirectEmailModel(
            "example@test.com",
            "This is a test email",
            "Default",
            "%%EMAIL_HEADER%%",
            "This is the emailbody",
            "this is the email footer",
            "Felix",
            "PetClinic"
        );
        yield return new DirectEmailModel(
            "example@test.com",
            "This is a test email",
            "Default",
            "Test email header",
            "%%EMAIL_BODY%%",
            "Test email footer",
            "John",
            "CompanyXYZ"
        );
        
        
        
        yield return new DirectEmailModel(
            "example@test.com",
            "This is a test email",
            "Default",
            "Test email header",
            "Test email body",
            "%%EMAIL_FOOTER%%",
            "John",
            "CompanyXYZ"
        );
        yield return new DirectEmailModel(
            "example@test.com",
            "This is a test email",
            "Default",
            "This is the emailHeader",
            "This is the emailbody",
            "this is the email footer",
            "%%EMAIL_NAME%%",
            "PetClinic"
        );

        yield return new DirectEmailModel(
            "example@test.com",
            "Another test email",
            "Default",
            "Test email header",
            "Test email body",
            "Test email footer",
            "John",
            "%%EMAIL_SENDER%%"
        );
    }
    [Test, TestCaseSource(nameof(FieldContainsPlaceHolder))]
    public async Task SendEmail_RequestFieldContainPlaceholder_ReturnsBadRequest(DirectEmailModel emailModelParam)
    {
        EmailUtils.EmailTemplates.Add(new EmailTemplate("Default","<html><body>%%EMAIL_HEADERS%% %%EMAIL_BODY%% %%EMAIL_FOOTER%% %%EMAIL_NAME%% %%EMAIL_SENDER%%</body></html>"));
        // Arrange
        var json = JsonConvert.SerializeObject(emailModelParam);

        var request = new HttpRequestMessage(HttpMethod.Post, "email/send")
        {
            Content = new StringContent(json, Encoding.UTF8, "application/json")
        };
        var response = await _httpClient.SendAsync(request);
        // Act
        // Assert
        Assert.AreEqual(HttpStatusCode.BadRequest, response.StatusCode);
    }*/
    public static IEnumerable<DirectEmailModel> TemplateRequiredFieldNotSet()
    {
        /*yield return new DirectEmailModel(
            "example@test.com",
            "This is a test email",
            "Default",
            "",
            "This is the emailbody",
            "this is the email footer",
            "Felix",
            "PetClinic"
        );*/
        yield return new DirectEmailModel(
            "example@test.com",
            "This is a test email",
            "Default",
            "Test email header",
            null!,
            "Test email footer",
            "John",
            "CompanyXYZ"
        );
        
        yield return new DirectEmailModel(
            "example@test.com",
            "This is a test email",
            "Default",
            "Test email header",
            "Test email body",
            "",
            "John",
            "CompanyXYZ"
        );
        yield return new DirectEmailModel(
            "example@test.com",
            "This is a test email",
            "Default",
            "This is the emailHeader",
            "This is the emailbody",
            "this is the email footer",
            "",
            "PetClinic"
        );

        yield return new DirectEmailModel(
            "example@test.com",
            "Another test email",
            "Default",
            "Test email header",
            "Test email body",
            "Test email footer",
            "John",
            null!
        );
    }
    [Test, TestCaseSource(nameof(TemplateRequiredFieldNotSet))]
    public async Task SendEmail_RequestFieldNeededIsNotSet_ReturnsBadRequest(DirectEmailModel emailModelParam)
    {
        EmailUtils.EmailTemplates.Add(new EmailTemplate("Default","<html><body>%%EMAIL_HEADERS%% %%EMAIL_BODY%% %%EMAIL_FOOTER%% %%EMAIL_NAME%% %%EMAIL_SENDER%%</body></html>"));
        // Arrange
        var json = JsonConvert.SerializeObject(emailModelParam);

        var request = new HttpRequestMessage(HttpMethod.Post, "email/send")
        {
            Content = new StringContent(json, Encoding.UTF8, "application/json")
        };
        var response = await _httpClient.SendAsync(request);
        // Act
        // Assert
        Assert.That(response.StatusCode, Is.EqualTo(HttpStatusCode.BadRequest));
    }
    
    
    
    
    [TearDown]
    public void AllTimeTearDown()
    {
        EmailUtils.EmailTemplates.Clear();
    }
    
    [OneTimeTearDown]
    public void TearDown()
    {
        _httpClient.Dispose();
    }
    // Test methods go here
}