using BrewMapEndpointUnitTest.ViewModels.Auth;
using Microsoft.Extensions.Configuration;
using System.Net.Http.Headers;
using System.Net.Http.Json;
using System.Text.Json;

namespace BrewMapEndpointUnitTest.Infrastructure;

public class ApiClient
{
    private static readonly JsonSerializerOptions JsonOptions = new()
    {
        PropertyNameCaseInsensitive = true
    };

    public string BaseUrl { get; }

    public string ValidAdminUsername { get; }
    public string ValidAdminPassword { get; }
    public string ValidAdminEmail { get; }

    public string ValidUserUsername { get; }
    public string ValidUserPassword { get; }
    public string ValidUserEmail { get; }

    public string ValidDrinkId { get; }
    public string ValidPaymentOptionTag { get; }
    public string ValidCategoryTag { get; }

    public ApiClient()
    {
        var config = new ConfigurationBuilder()
            .SetBasePath(Directory.GetCurrentDirectory())
            .AddJsonFile("appsettings.json", optional: false)
            .Build();

        BaseUrl = config["ApiSettings:BaseUrl"]
            ?? throw new InvalidOperationException("ApiSettings:BaseUrl is not configured");

        ValidAdminUsername =    config["ApiSettings:ValidCredentialsAdmin:Username"]    ?? "admin";
        ValidAdminPassword =    config["ApiSettings:ValidCredentialsAdmin:Password"]    ?? "Password1!";
        ValidAdminEmail =       config["ApiSettings:ValidCredentialsAdmin:Email"]       ?? "admin@brewmap.dev";

        ValidUserUsername =     config["ApiSettings:ValidCredentialsUser:Username"]     ?? "user";
        ValidUserPassword =     config["ApiSettings:ValidCredentialsUser:Password"]     ?? "Password1!";
        ValidUserEmail =        config["ApiSettings:ValidCredentialsUser:Email"]        ?? "admin@brewmap.dev";

        ValidDrinkId =              config["ApiSettings:TestData:ValidDrinkId"]             ?? string.Empty;
        ValidPaymentOptionTag =     config["ApiSettings:TestData:ValidPaymentOptionTag"]    ?? "cash";
        ValidCategoryTag =          config["ApiSettings:TestData:ValidCategoryTag"]         ?? "coffee";
    }

    public string GetUrl(string path) => $"{BaseUrl.TrimEnd('/')}/{path.TrimStart('/')}";

    public async Task<string> GetAdminTokenAsync()
    {
        using var client = Create();
        var response = await client.PostAsJsonAsync(GetUrl("auth/login"), new LoginRequestViewModel
        {
            Username = ValidAdminUsername,
            Password = ValidAdminPassword
        });
        var body = await response.Content.ReadFromJsonAsync<AuthResponseViewModel>(GetJsonOptions());
        return body!.Token!;
    }

    public async Task<string> GetUserTokenAsync()
    {
        using var client = Create();
        var response = await client.PostAsJsonAsync(GetUrl("auth/login"), new LoginRequestViewModel
        {
            Username = ValidUserUsername,
            Password = ValidUserPassword
        });
        var body = await response.Content.ReadFromJsonAsync<AuthResponseViewModel>(GetJsonOptions());
        return body!.Token!;
    }

    public HttpClient Create() => new();

    public HttpClient CreateAuthenticated(string token)
    {
        var client = new HttpClient();
        client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", token);
        return client;
    }

    public static JsonSerializerOptions GetJsonOptions() => JsonOptions;
}
