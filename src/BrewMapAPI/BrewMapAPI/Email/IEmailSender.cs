namespace BrewMapAPI.Email;

public interface IEmailSender
{
    Task SendEmailAsync(Message message);
}