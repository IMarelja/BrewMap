using System.Net;
using System.Net.Http.Json;
using BrewMapEndpointUnitTest.Infrastructure;
using BrewMapEndpointUnitTest.ViewModels.Auth;
using FluentAssertions;
using Xunit;

namespace BrewMapEndpointUnitTest.Tests;

public class AuthControllerTests
{
    private readonly ApiClient _apiClient = new();

    [Fact]
    public async Task Login_ValidCredentials_Returns200WithToken()
    {
        using var client = _apiClient.Create();
        var request = new LoginRequestViewModel
        {
            Username = _apiClient.ValidAdminUsername,
            Password = _apiClient.ValidAdminPassword
        };

        var response = await client.PostAsJsonAsync(_apiClient.GetUrl("auth/login"), request);
        var body = await response.Content.ReadFromJsonAsync<AuthResponseViewModel>(ApiClient.GetJsonOptions());

        response.StatusCode.Should().Be(HttpStatusCode.OK);
        body.Should().NotBeNull();
        body!.Success.Should().BeTrue();
        body.Token.Should().NotBeNullOrEmpty();
    }

    [Fact]
    public async Task Login_InvalidPassword_Returns401()
    {
        using var client = _apiClient.Create();
        var request = new LoginRequestViewModel
        {
            Username = _apiClient.ValidAdminUsername,
            Password = "wrongpassword"
        };

        var response = await client.PostAsJsonAsync(_apiClient.GetUrl("auth/login"), request);
        var body = await response.Content.ReadFromJsonAsync<AuthResponseViewModel>(ApiClient.GetJsonOptions());

        response.StatusCode.Should().Be(HttpStatusCode.Unauthorized);
        body.Should().NotBeNull();
        body!.Success.Should().BeFalse();
        body.Token.Should().BeNullOrEmpty();
    }

    [Fact]
    public async Task Login_NonExistentUser_Returns401()
    {
        using var client = _apiClient.Create();
        var request = new LoginRequestViewModel
        {
            Username = $"ghost_{Guid.NewGuid():N}",
            Password = "anypassword"
        };

        var response = await client.PostAsJsonAsync(_apiClient.GetUrl("auth/login"), request);
        var body = await response.Content.ReadFromJsonAsync<AuthResponseViewModel>(ApiClient.GetJsonOptions());

        response.StatusCode.Should().Be(HttpStatusCode.Unauthorized);
        body.Should().NotBeNull();
        body!.Success.Should().BeFalse();
    }

    [Fact]
    public async Task Register_NewUser_Returns200WithToken()
    {
        using var client = _apiClient.Create();
        var suffix = Guid.NewGuid().ToString("N")[..8];
        var request = new RegisterRequestViewModel
        {
            Email = $"test_{suffix}@brewmap.test",
            Username = $"testuser_{suffix}",
            Password = "Token check"
        };

        var response = await client.PostAsJsonAsync(_apiClient.GetUrl("auth/register"), request);
        var body = await response.Content.ReadFromJsonAsync<AuthResponseViewModel>(ApiClient.GetJsonOptions());

        response.StatusCode.Should().Be(HttpStatusCode.OK);
        body.Should().NotBeNull();
        body!.Success.Should().BeTrue();
        body.Token.Should().NotBeNullOrEmpty();
    }

    [Fact]
    public async Task Register_DuplicateUsername_Returns401WithFailure()
    {
        using var client = _apiClient.Create();
        var suffix = Guid.NewGuid().ToString("N")[..8];

        var username = $"duptest_{suffix}";

        var firstRequest = new RegisterRequestViewModel
        {
            Email = $"first_{suffix}@brewmap.test",
            Username = username,
            Password = "validPassowrd"
        };
        await client.PostAsJsonAsync(_apiClient.GetUrl("auth/register"), firstRequest);

        var secondRequest = new RegisterRequestViewModel
        {
            Email = $"second_{suffix}@brewmap.test",
            Username = username,
            Password = "validPassowrd"
        };
        var response = await client.PostAsJsonAsync(_apiClient.GetUrl("auth/register"), secondRequest);
        var body = await response.Content.ReadFromJsonAsync<AuthResponseViewModel>(ApiClient.GetJsonOptions());

        response.StatusCode.Should().Be(HttpStatusCode.Unauthorized);
        body.Should().NotBeNull();
        body!.Success.Should().BeFalse();
        body.Message.Should().Contain("taken");
    }

    [Fact]
    public async Task Register_InvalidEmail_Returns400()
    {
        using var client = _apiClient.Create();
        var request = new RegisterRequestViewModel
        {
            Email = "not-an-email",
            Username = $"testuser_{Guid.NewGuid():N}",
            Password = "_apiClient"
        };

        var response = await client.PostAsJsonAsync(_apiClient.GetUrl("auth/register"), request);

        response.StatusCode.Should().Be(HttpStatusCode.BadRequest);
    }
}
