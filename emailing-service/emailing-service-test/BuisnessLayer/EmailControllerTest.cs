using System.Net.Http;
using emailing_service.BuisnessLayer;
using emailing_service.Controllers;
using emailing_service.Models;
using emailing_service.Models.EmailType;
using emailing_service.Utils;
using emailing_service.Utils.Exception;
using Microsoft.AspNetCore.Http;
using Microsoft.Extensions.DependencyInjection;

namespace emailing_service_test.BuisnessLayer;

using NUnit.Framework;
using Moq;
using Microsoft.AspNetCore.Mvc;
using System.Threading.Tasks;
using System.IO;
using System.Text;

public class EmailControllerTests
{
    private IEmailService _controller;
    private readonly string pathOfDefaultHtml;
    private readonly DirectEmailModel directEmailModel;

    public EmailControllerTests()
    {
        
        pathOfDefaultHtml = "<html><body>%%EMAIL_HEADERS%% %%EMAIL_BODY%% %%EMAIL_FOOTER%% %%EMAIL_NAME%% %%EMAIL_SENDER%%</body></html>";
    }
    [OneTimeSetUp]
    public async Task SetUp()
    {
        _controller = new EmailServiceImpl();
    }
    /*[Test]
    public async Task TestEndpoint_Returns_OkObjectResult()
    {
        var result = _controller.;
        Assert.That(result, Is.InstanceOf<OkResult>());
    }*/

    
    [Test]
    [TestCase("T")]
    [TestCase("Template2")]
    [TestCase("ndawindawiuhduiaw")]
    public async Task ReceiveHtml_Returns_OperationResult(string templateName)
    {
        var result = _controller.ReceiveHtml(templateName, pathOfDefaultHtml);
        Assert.That(result, Is.TypeOf<OperationResult>());
        OperationResult operationResult = (OperationResult)result;
        Assert.That(operationResult.IsSuccess, Is.True);
    }
    [Test]
    [TestCase("")]
    [TestCase(null)]
    public void ReceiveHtml_NullOrEmptyName_ThrowsTemplateFormatException(string? templateName)
    {
        // Act & Assert
        var ex = Assert.Throws<TemplateFormatException>(() =>
            _controller.ReceiveHtml(templateName, pathOfDefaultHtml));
    
        Assert.That(ex.Message, Does.Contain("Template Name is required"));
    }
    
    [Test]
    [TestCase("")]
    [TestCase(null)]
    public void ReceiveHtml_NullOrEmptyHtmlBody_ThrowsTemplateFormatException(string? templateBody)
    {
        // Act & Assert
        var ex = Assert.Throws<TemplateFormatException>(() =>
            _controller.ReceiveHtml("templateName", templateBody));
    
        Assert.That(ex.Message, Does.Contain("HTML content was missing"));
    }
    [Test]
    [TestCase("FirstExample1")]
    [TestCase("Example1")]
    public void ReceiveHtml_AlreadyExistingTemplate_ThrowsTemplateFormatException(string? templateName)
    {
        //Create our first template
        var result = _controller.ReceiveHtml(templateName, pathOfDefaultHtml);
        Assert.That(result, Is.TypeOf<OperationResult>());
        OperationResult operationResult = (OperationResult)result;
        Assert.That(operationResult.IsSuccess, Is.True);

        
        // Act & Assert
        var ex = Assert.Throws<TemplateFormatException>(() =>
            _controller.ReceiveHtml(templateName, pathOfDefaultHtml));
    
        Assert.That(ex.Message, Does.Contain($"Template [{templateName}] already exists."));
    }

    [Test]
    public void SendEmail_ValidEmail_SendsEmail()
    {
        
    }
    
    /*[Test]
    public async Task ReceiveHtml_AlreadyExistingTemplate_BadRequest()
    {
        //We add a template
        _controller.ReceiveHtml("TEMPLATE");
        var result = _controller.ReceiveHtml("NewTemplate");
        Assert.That(result, Is.InstanceOf<OkResult>());
    }*/
    
    
    
    
    
    
    [OneTimeTearDown]
    public void TearDown()
    {
        EmailUtils.EmailTemplates.Clear();
    }
}
