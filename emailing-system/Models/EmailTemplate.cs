using emailing_system.Utils.Exception;

namespace emailing_system.Models;

// &copy; 2024 Your Company. All rights reserved.
public class EmailTemplate
{
    public string Name => _name;

    public string HtmlFormat => _htmlFormat;
    public EmailTemplate(string name, string htmlFormat)
    {
        this._name = name;
        this._htmlFormat = htmlFormat;
        if(htmlFormat.Contains("%%EMAIL_HEADER%%"))
            _doesHaveHeader = true;
        if(htmlFormat.Contains("%%EMAIL_BODY%%"))
            _doesHaveBody = true;
        if(htmlFormat.Contains("%%EMAIL_FOOTER%%"))
            _doesHaveFooter = true;
        if(htmlFormat.Contains("%%EMAIL_NAME%%"))
            _doesHaveName = true;
        if(htmlFormat.Contains("%%EMAIL_SENDER%%"))
            _doesHaveSender = true;
    }
    // On template Build
    private readonly string _name;
    private readonly string _htmlFormat;
    
    private readonly bool _doesHaveHeader; // %%EMAIL_HEADER
    private readonly bool _doesHaveBody; // %%EMAIL_BODY%%
    private readonly bool _doesHaveFooter; // %%EMAIL_FOOTER%%
    private readonly bool _doesHaveName; // %%EMAIL_NAME%%
    private readonly bool _doesHaveSender; // %%EMAIL_SENDER%%

    public string BuildEmail(
        string header = "", 
        string body = "", 
        string footer = "", 
        string correspondentName = "", 
        string sender = "")
    {
        
        string emailContent = HtmlFormat;
        if (_doesHaveHeader)
        {
            if (header != "")
            {
                emailContent = emailContent.Replace("%%EMAIL_HEADER%%", header);
            }
            else
            {
                throw new TemplateRequiredFieldNotSet(nameof(header));
            }
        }

        if (_doesHaveBody)
        {
            if (body != "")
            {
                emailContent = emailContent.Replace("%%EMAIL_BODY%%", body);
            }
            else
            {
                throw new TemplateRequiredFieldNotSet(nameof(body));    
            }
        }

        if (_doesHaveFooter)
        {
            if (footer != "")
            {
                emailContent = emailContent.Replace("%%EMAIL_FOOTER%%", footer);
            }
            else
            {
                throw new TemplateRequiredFieldNotSet(nameof(footer));
            }
        }

        if (_doesHaveName)
        {
            if (correspondentName != "")
            {
                emailContent = emailContent.Replace("%%EMAIL_NAME%%", correspondentName);
            }
            else
            {
                throw new TemplateRequiredFieldNotSet(nameof(correspondentName));
            }
        }

        if (_doesHaveSender)
        {
            if (sender != "")
            {
                emailContent = emailContent.Replace("%%EMAIL_SENDER%%", sender);
            }
            else
            {
                throw new TemplateRequiredFieldNotSet(nameof(sender));
            }
        }
        
        return emailContent;
        
    }
    
}