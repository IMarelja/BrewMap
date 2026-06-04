using BrewMapEndpointUnitTest.ViewModels.Auth;
using Microsoft.Extensions.Configuration;
using System.Net.Http.Headers;
using System.Net.Http.Json;
using System.Text.Json;
using System.Globalization;

namespace BrewMapEndpointUnitTest.Infrastructure;

public class ApiClient
{
    private static readonly JsonSerializerOptions JsonOptions = new()
    {
        PropertyNameCaseInsensitive = true
    };

    public string BaseUrl { get; }

    public string? ValidAdminUsername { get; }
    public string? ValidAdminPassword { get; }
    public string? ValidAdminEmail { get; }

    public string? ValidUserUsername { get; }
    public string? ValidUserPassword { get; }
    public string? ValidUserEmail { get; }

    public string? ValidUserId { get; }
    public string? ValidAdminId { get; }
    public string? ValidLocationId { get; }
    public string? ValidDrinkId { get; }
    public string? ValidPaymentOptionTag { get; }
    public string? ValidCategoryTag { get; }
    public string? ValidFlagId { get; }
    public double SearchLongitude { get; }
    public double SearchLatitude { get; }
    public double SearchRadiusMeters { get; }
    public bool HasSearchLongitude { get; }
    public bool HasSearchLatitude { get; }
    public bool HasSearchRadiusMeters { get; }
    


    public ApiClient()
    {
        var config = new ConfigurationBuilder()
            .SetBasePath(Directory.GetCurrentDirectory())
            .AddJsonFile("appsettings.json", optional: false)
            .Build();

        BaseUrl = config["ApiSettings:BaseUrl"]
            ?? throw new InvalidOperationException("ApiSettings:BaseUrl is not configured");

        ValidAdminUsername =    config["ApiSettings:ValidCredentialsAdmin:Username"];
        ValidAdminPassword =    config["ApiSettings:ValidCredentialsAdmin:Password"];
        ValidAdminEmail =       config["ApiSettings:ValidCredentialsAdmin:Email"];

        ValidUserUsername =     config["ApiSettings:ValidCredentialsUser:Username"];
        ValidUserPassword =     config["ApiSettings:ValidCredentialsUser:Password"];
        ValidUserEmail =        config["ApiSettings:ValidCredentialsUser:Email"];;

        ValidUserId =               config["ApiSettings:TestData:ValidUserId"];
        ValidAdminId =              config["ApiSettings:TestData:ValidAdminId"];
        ValidLocationId =           config["ApiSettings:TestData:ValidLocationId"];
        ValidDrinkId =              config["ApiSettings:TestData:ValidDrinkId"];
        ValidPaymentOptionTag =     config["ApiSettings:TestData:ValidPaymentOptionTag"];
        ValidCategoryTag =          config["ApiSettings:TestData:ValidCategoryTag"];
        ValidFlagId =               config["ApiSettings:TestData:ValidFlagId"];

        var searchLongitudeRaw = config["ApiSettings:TestData:Coordinates:0"];
        var searchLatitudeRaw = config["ApiSettings:TestData:Coordinates:1"];
        var searchRadiusMetersRaw = config["ApiSettings:TestData:SearchRadiusMeters"];

        HasSearchLongitude = !string.IsNullOrWhiteSpace(searchLongitudeRaw);
        HasSearchLatitude = !string.IsNullOrWhiteSpace(searchLatitudeRaw);
        HasSearchRadiusMeters = !string.IsNullOrWhiteSpace(searchRadiusMetersRaw);

        SearchLongitude = ReadDouble(searchLongitudeRaw, 15.9819);
        SearchLatitude = ReadDouble(searchLatitudeRaw, 45.8150);
        SearchRadiusMeters = ReadDouble(searchRadiusMetersRaw, 3000);
    }

    public string GetUrl(string path) => $"{BaseUrl.TrimEnd('/')}/{path.TrimStart('/')}";

    public async Task<string> GetAdminTokenAsync()
    {
        using var client = Create();
        var response = await client.PatchAsJsonAsync(GetUrl("auth/login"), new LoginRequestViewModel
        {
            User = ValidAdminUsername,
            Password = ValidAdminPassword
        });
        var body = await response.Content.ReadFromJsonAsync<AuthResponseViewModel>(GetJsonOptions());
        return body!.Token!;
    }

    public async Task<string> GetUserTokenAsync()
    {
        using var client = Create();
        var response = await client.PatchAsJsonAsync(GetUrl("auth/login"), new LoginRequestViewModel
        {
            User = ValidUserUsername,
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

    private static double ReadDouble(string? rawValue, double fallback)
    {
        if (double.TryParse(rawValue, NumberStyles.Float, CultureInfo.InvariantCulture, out var value))
            return value;

        return fallback;
    }
}
