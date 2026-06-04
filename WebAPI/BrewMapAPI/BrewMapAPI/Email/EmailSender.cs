using MailKit.Net.Smtp;
using MimeKit;

namespace BrewMapAPI.Email;

public class EmailSender : IEmailSender
{
    private readonly EmailConfiguration _emailConfig;
    public EmailSender(EmailConfiguration emailConfig)
    {
        _emailConfig = emailConfig;
    }
    public async Task SendEmailAsync(Message message)
    {
        var mailMessage = CreateEmailMessage(message);
        await SendAsync(mailMessage);
    }
    
    private MimeMessage CreateEmailMessage(Message message)
    {
        var emailMessage = new MimeMessage();
        emailMessage.From.Add(new MailboxAddress("email", _emailConfig.From));
        emailMessage.To.AddRange(message.To);
        emailMessage.Subject = message.Subject;
        emailMessage.Body = new TextPart(MimeKit.Text.TextFormat.Text) { Text = message.Content };
        return emailMessage;
    }
    private async Task SendAsync(MimeMessage mailMessage)
    {
        using (var client = new SmtpClient())
        {
            try
            {
                await client.ConnectAsync(_emailConfig.SmtpServer, _emailConfig.Port, true);
                client.AuthenticationMechanisms.Remove("XOAUTH2");
                await client.AuthenticateAsync(_emailConfig.UserName, _emailConfig.Password);
                await client.SendAsync(mailMessage);
            }
            catch(Exception e)
            {
                throw new Exception(e.Message);
            }
            finally
            {
                await client.DisconnectAsync(true);
                client.Dispose();
            }
        }
    }
}