using System.Text.Json.Serialization;

namespace BrewMapEndpointUnitTest.ViewModels.Auth;

public class LoginRequestViewModel
{
    [JsonPropertyName("user")]
    public string User { get; set; } = string.Empty;

    [JsonPropertyName("password")]
    public string Password { get; set; } = string.Empty;

    [JsonPropertyName("rememberMe")]
    public bool RememberMe { get; set; } = false;
}
