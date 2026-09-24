namespace BrewMapEndpointUnitTest.ViewModels.Auth;

public class AuthResponseViewModel
{
    public bool Success { get; set; }
    public string? Token { get; set; }
    public string Message { get; set; } = string.Empty;
    public int StatusCode { get; set; }
}
