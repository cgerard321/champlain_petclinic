using emailing_service.Models;
using emailing_service.Utils.Exception;
using NUnit.Framework;

namespace emailing_service_test.Models;
[TestFixture]
public class EmailTemplateTests
    {
        [Test]
        public void BuildEmail_ReturnsCorrectEmailContent_WhenAllFieldsProvided()
        {
            // Arrange
            var template = new EmailTemplate("TestTemplate", "Header: %%EMAIL_HEADER%%\nBody: %%EMAIL_BODY%%\nFooter: %%EMAIL_FOOTER%%");
            string header = "Welcome";
            string body = "Hello, this is the body.";
            string footer = "Regards,";
            string correspondentName = "John Doe";
            string sender = "sender@example.com";

            // Act
            var result = template.BuildEmail(header, body, footer, correspondentName, sender);

            // Assert
            Assert.That(result, Is.EqualTo("Header: Welcome\nBody: Hello, this is the body.\nFooter: Regards,"));
        }

        [Test]
        public void BuildEmail_ThrowsTemplateRequiredFieldNotSet_WhenHeaderIsEmpty()
        {
            // Arrange
            var template = new EmailTemplate("TestTemplate", "Header: %%EMAIL_HEADER%%");

            // Act & Assert
            var ex = Assert.Throws<TemplateRequiredFieldNotSet>(() => template.BuildEmail("", "Body", "Footer"));
            Assert.That(ex.Message, Contains.Substring("header"));
        }

        [Test]
        public void BuildEmail_ThrowsEmailStringContainsPlaceholder_WhenHeaderContainsPlaceholder()
        {
            // Arrange
            var template = new EmailTemplate("TestTemplate", "Header: %%EMAIL_HEADER%%");

            // Act & Assert
            var ex = Assert.Throws<EmailStringContainsPlaceholder>(() => template.BuildEmail("Some header with %%EMAIL_HEADER%%", "Body", "Footer"));
            Assert.That(ex.Message, Contains.Substring("header"));
        }

        [Test]
        public void BuildEmail_ThrowsTemplateRequiredFieldNotSet_WhenBodyIsEmpty()
        {
            // Arrange
            var template = new EmailTemplate("TestTemplate", "Body: %%EMAIL_BODY%%");

            // Act & Assert
            var ex = Assert.Throws<TemplateRequiredFieldNotSet>(() => template.BuildEmail("Header", "", "Footer"));
            Assert.That(ex.Message, Contains.Substring("body"));
        }

        [Test]
        public void BuildEmail_ThrowsTemplateRequiredFieldNotSet_WhenFooterIsEmpty()
        {
        // Arrange
            var template = new EmailTemplate("TestTemplate", "Footer: %%EMAIL_FOOTER%%");

            // Act & Assert
            var ex = Assert.Throws<TemplateRequiredFieldNotSet>(() => template.BuildEmail("Header", "Body", ""));
            Assert.That(ex.Message, Contains.Substring("footer"));
        }



        [Test]
        public void BuildEmail_ThrowsTemplateRequiredFieldNotSet_WhenCorrespondentNameIsEmpty()
        {
        // Arrange
        var template = new EmailTemplate("TestTemplate", "Name: %%EMAIL_NAME%%");

        // Act & Assert
        var ex = Assert.Throws<TemplateRequiredFieldNotSet>(() => template.BuildEmail("Header", "Body", "Footer", ""));
        Assert.That(ex.Message, Contains.Substring("correspondentName"));
        }
        
        [Test]
        public void BuildEmail_ThrowsTemplateRequiredFieldNotSet_WhenSenderIsEmpty()
        {
        // Arrange
        var template = new EmailTemplate("TestTemplate", "Sender: %%EMAIL_SENDER%%");

        // Act & Assert
        var ex = Assert.Throws<TemplateRequiredFieldNotSet>(() => template.BuildEmail("Header", "Body", "Footer", "Name", ""));
        Assert.That(ex.Message, Contains.Substring("sender"));
        }


        // Add more tests for footer, correspondentName, and sender as needed
    }