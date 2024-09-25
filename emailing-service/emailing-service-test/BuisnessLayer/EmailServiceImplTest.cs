using emailing_service_test.Models.Database;
using emailing_service.BuisnessLayer;
using emailing_service.Models;
using emailing_service.Models.Database;
using emailing_service.Models.EmailType;
using emailing_service.Utils;
using emailing_service.Utils.Exception;

namespace emailing_service_test.BuisnessLayer;

using NUnit.Framework;
using System.Threading.Tasks;


[TestFixture]
public class EmailServiceImplTest
{
    private IEmailService _controller;
    private readonly string _pathOfDefaultHtml;
    public EmailServiceImplTest()
    {
        EmailUtils.sendEmail = false;
        _pathOfDefaultHtml = "<html><body>%%EMAIL_HEADERS%% %%EMAIL_BODY%% %%EMAIL_FOOTER%% %%EMAIL_NAME%% %%EMAIL_SENDER%%</body></html>";
    }
    
    [OneTimeSetUp]
    public Task SetUp()
    {
        EmailUtils.emailConnectionString  = new ConnectionEmailServer(
            "Mock",
            000,
            "Mock",
            "Mock",
            "mockemail@gmail.com",
            "MockPetClinic"
        );
        _controller = new EmailServiceImpl();
        _controller.SetDatabaseHelper(new TestDbContext());
        return Task.CompletedTask;
    }
    /*[Test]
    public async Task TestEndpoint_Returns_OkObjectResult()
    {
        var result = _controller.;
        Assert.That(result, Is.InstanceOf<OkResult>());
    }*/

    [Test]
    public Task GetAll_ReturnAllMessages_ReturnAllEmails()
    {
        var result = _controller.GetAllEmails();
        Assert.That(result, Is.Not.Null);
        Assert.IsAssignableFrom<List<EmailModel>>(result);
        return Task.CompletedTask;
    }
    
    [Test]
    public Task GetAll_AbscentDatabase_ThrowMissingDatabaseException()
    {
        IEmailService service = new EmailServiceImpl();
        Assert.Throws<MissingDatabaseException>(() => {
            service.GetAllEmails();
        });
        return Task.CompletedTask;
    }
    
    [Test]
    [TestCase("T")]
    [TestCase("Template2")]
    [TestCase("ndawindawiuhduiaw")]
    public Task ReceiveHtml_Returns_OperationResult(string templateName)
    {
        var result = _controller.ReceiveHtml(templateName, _pathOfDefaultHtml);
        Assert.That(result, Is.TypeOf<OperationResult>());
        OperationResult operationResult = result;
        Assert.That(operationResult.IsSuccess, Is.True);
        return Task.CompletedTask;
    }
    [Test]
    [TestCase("")]
    [TestCase(null)]
    public void ReceiveHtml_NullOrEmptyName_ThrowsTemplateFormatException(string? templateName)
    {
        // Act & Assert
        var ex = Assert.Throws<TemplateFormatException>(() =>
            _controller.ReceiveHtml(templateName!, _pathOfDefaultHtml));
    
        Assert.That(ex.Message, Does.Contain("Template Name is required"));
    }
    
    [Test]
    [TestCase("")]
    [TestCase(null)]
    public void ReceiveHtml_NullOrEmptyHtmlBody_ThrowsTemplateFormatException(string? templateBody)
    {
        // Act & Assert
        var ex = Assert.Throws<TemplateFormatException>(() =>
            _controller.ReceiveHtml("templateName", templateBody!));
    
        Assert.That(ex.Message, Does.Contain("HTML content was missing"));
    }
    [Test]
    [TestCase("FirstExample1")]
    [TestCase("Example1")]
    public void ReceiveHtml_AlreadyExistingTemplate_ThrowsTemplateFormatException(string templateName)
    {
        //Create our first template
        var result = _controller.ReceiveHtml(templateName, _pathOfDefaultHtml);
        Assert.That(result, Is.TypeOf<OperationResult>());
        OperationResult operationResult = result;
        Assert.That(operationResult.IsSuccess, Is.True);

        
        // Act & Assert
        var ex = Assert.Throws<TemplateFormatException>(() =>
            _controller.ReceiveHtml(templateName, _pathOfDefaultHtml));
    
        Assert.That(ex.Message, Does.Contain($"Template [{templateName}] already exists."));
    }
    
    public static IEnumerable<DirectEmailModel> ValidEmailModels()
    {
        yield return new DirectEmailModel(
            "xilef992@gmail.com",
            "This is a test email",
            "Default",
            "This is the emailHeader",
            "This is the email body",
            "this is the email footer",
            "Felix",
            "PetClinic"
        );

        yield return new DirectEmailModel(
            "example2@test.com",
            "Another test email",
            null!,
            "Test email header",
            "Test email body",
            "Test email footer",
            "John",
            "CompanyXYZ"
        );
    }
    [Test, TestCaseSource(nameof(ValidEmailModels))]
    public void SendEmail_ValidEmail_SendsEmail(DirectEmailModel testModel)
    {
        EmailUtils.EmailTemplates.Clear();
        var templateCreateResult = _controller.ReceiveHtml("Default", _pathOfDefaultHtml);
        Assert.That(templateCreateResult, Is.TypeOf<OperationResult>());
        OperationResult operationResultForTemplateCreate = templateCreateResult;
        Assert.That(operationResultForTemplateCreate.IsSuccess, Is.True);
        
        
        var result = _controller.SendEmail(testModel);
        Assert.That(result, Is.TypeOf<OperationResult>());
        OperationResult operationResult = result;
        Assert.That(operationResult.IsSuccess, Is.True);
        
    }
    /*
     We now do this in the endpoint
     [Test]
    [TestCase(null)]
    public void SendEmail_InvalidTemplateName_TemplateFormatException(DirectEmailModel? testModel)
    {
        var ex = Assert.Throws<MissingBodyException>(() =>
            _controller.SendEmail(testModel));
    
        Assert.That(ex.Message, Does.Contain("Email Model is null"));
        
    }*/
    public static IEnumerable<DirectEmailModel> NullOrWhiteSpaceEmailModels()
    {
        yield return new DirectEmailModel(
            null!,
            "This is a test email",
            "Default",
            "This is the emailHeader",
            "This is the emailbody",
            "this is the email footer",
            "Felix",
            "PetClinic"
        );

        yield return new DirectEmailModel(
            "",
            "Another test email",
            "Default",
            "Test email header",
            "Test email body",
            "Test email footer",
            "John",
            "CompanyXYZ"
        );
        yield return new DirectEmailModel(
            "               ",
            "Another test email",
            "Default",
            "Test email header",
            "Test email body",
            "Test email footer",
            "John",
            "CompanyXYZ"
        );
    }
    [Test, TestCaseSource(nameof(NullOrWhiteSpaceEmailModels))]
    public void SendEmail_NullOrWhitespaceEmailToSendTo_BadEmailModel(DirectEmailModel testModel)
    {
        var ex = Assert.Throws<BadEmailModel>(() =>
            _controller.SendEmail(testModel));
        Assert.That(ex.Message, Does.Contain("Email To Send To is null or whitespace. EMAIL IS REQUIRED"));
    }
    
    public static IEnumerable<DirectEmailModel> InvalidEmailEmailModels()
    {
        yield return new DirectEmailModel(
            "@gmail.com",
            "This is a test email",
            "Default",
            "This is the emailHeader",
            "This is the emailbody",
            "this is the email footer",
            "Felix",
            "PetClinic"
        );
        yield return new DirectEmailModel(
            "felix@gmail",
            "Another test email",
            "Default",
            "Test email header",
            "Test email body",
            "Test email footer",
            "John",
            "CompanyXYZ"
        );
        yield return new DirectEmailModel(
            "felix@@gmail.com",
            "Another test email",
            "Default",
            "Test email header",
            "Test email body",
            "Test email footer",
            "John",
            "CompanyXYZ"
        );
        yield return new DirectEmailModel(
            "felix@.com",
            "Another test email",
            "Default",
            "Test email header",
            "Test email body",
            "Test email footer",
            "John",
            "CompanyXYZ"
        );
        yield return new DirectEmailModel(
            "@gmail.com",
            "Another test email",
            "Default",
            "Test email header",
            "Test email body",
            "Test email footer",
            "John",
            "CompanyXYZ"
        );
        yield return new DirectEmailModel(
            "justin@dobbylechat@gmail.com",
            "Another test email",
            "Default",
            "Test email header",
            "Test email body",
            "Test email footer",
            "John",
            "CompanyXYZ"
        );
    }
    [Test, TestCaseSource(nameof(InvalidEmailEmailModels))]
    public void SendEmail_InvalidEmail_BadEmailModel(DirectEmailModel testModel)
    {
        var ex = Assert.Throws<BadEmailModel>(() =>
            _controller.SendEmail(testModel));
        Assert.That(ex.Message, Does.Contain("Email To Send To Not Valid"));
    }

    public static IEnumerable<DirectEmailModel> NullOrWhitespaceTitleEmailModels()
    {
        yield return new DirectEmailModel(
            "felix@gmail.com",
            null!,
            "Default",
            "This is the emailHeader",
            "This is the emailbody",
            "this is the email footer",
            "Felix",
            "PetClinic"
        );
        yield return new DirectEmailModel(
            "felix@gmail.com",
            "",
            "Default",
            "Test email header",
            "Test email body",
            "Test email footer",
            "John",
            "CompanyXYZ"
        );
        yield return new DirectEmailModel(
            "felix@gmail.com",
            "               ",
            "Default",
            "Test email header",
            "Test email body",
            "Test email footer",
            "John",
            "CompanyXYZ"
        );
    }
    [Test, TestCaseSource(nameof(NullOrWhitespaceTitleEmailModels))]
    public void SendEmail_NullOrWhitespaceTitle_BadEmailModel(DirectEmailModel testModel)
    {
        var ex = Assert.Throws<BadEmailModel>(() =>
            _controller.SendEmail(testModel));
        Assert.That(ex.Message, Does.Contain("Email Title is null or whitespace"));
    }
    
    public static IEnumerable<DirectEmailModel> NonExistingTemplateEmailModels()
    {
        yield return new DirectEmailModel(
            "felix@gmail.com",
            "Example1",
            "Example1OfNonExistingTemplate",
            "This is the emailHeader",
            "This is the emailbody",
            "this is the email footer",
            "Felix",
            "PetClinic"
        );
        yield return new DirectEmailModel(
            "felix@gmail.com",
            "Example2",
            "NotAnExistingTemplate",
            "Test email header",
            "Test email body",
            "Test email footer",
            "John",
            "CompanyXYZ"
        );
        yield return new DirectEmailModel(
            "felix@gmail.com",
            "Example3",
            "ThisTemplateDoesNotExist",
            "Test email header",
            "Test email body",
            "Test email footer",
            "John",
            "CompanyXYZ"
        );
    }
    [Test, TestCaseSource(nameof(NonExistingTemplateEmailModels))]
    public void SendEmail_NonExistingTemplate_TriedToFindNonExistingTemplate(DirectEmailModel testModel)
    {
        var ex = Assert.Throws<TriedToFindNonExistingTemplate>(() =>
            _controller.SendEmail(testModel));
        Assert.That(ex.Message,
            Does.Contain($"Template {testModel.TemplateName} does not exist. Please create a template first or use the default one (Default)"));
    }
    
    
    [OneTimeTearDown]
    public void TearDown()
    {
        EmailUtils.EmailTemplates.Clear();
    }
}
