using System.Net;
using System.Net.Http.Json;
using BrewMapEndpointUnitTest.Infrastructure;
using BrewMapEndpointUnitTest.ViewModels.Auth;
using BrewMapEndpointUnitTest.ViewModels.Drinks;
using FluentAssertions;
using Xunit;

namespace BrewMapEndpointUnitTest.Tests;

public class DrinkControllerTests
{
    private readonly ApiClient _apiClient = new();

    private async Task<string> GetTokenAsync()
    {
        using var client = _apiClient.Create();
        var loginRequest = new LoginRequestViewModel
        {
            Username = _apiClient.ValidAdminUsername,
            Password = _apiClient.ValidAdminPassword
        };
        var response = await client.PostAsJsonAsync(_apiClient.GetUrl("auth/login"), loginRequest);
        var body = await response.Content.ReadFromJsonAsync<AuthResponseViewModel>(ApiClient.GetJsonOptions());
        return body!.Token!;
    }

    [Fact]
    public async Task GetById_ValidIdWithToken_Returns200WithDrink()
    {
        var token = await GetTokenAsync();
        using var client = _apiClient.CreateAuthenticated(token);

        var response = await client.GetAsync(_apiClient.GetUrl($"Drink/{_apiClient.ValidDrinkId}"));
        var body = await response.Content.ReadFromJsonAsync<ReadDrinkViewModel>(ApiClient.GetJsonOptions());

        response.StatusCode.Should().Be(HttpStatusCode.OK);
        body.Should().NotBeNull();
        body!.Id.Should().Be(_apiClient.ValidDrinkId);
        body.Name.Should().NotBeNullOrEmpty();
    }

    [Fact]
    public async Task GetById_InvalidId_Returns400()
    {
        var token = await GetTokenAsync();
        using var client = _apiClient.CreateAuthenticated(token);

        var response = await client.GetAsync(_apiClient.GetUrl("drink/invalid-id-format"));

        response.StatusCode.Should().Be(HttpStatusCode.BadRequest);
    }

    [Fact]
    public async Task GetById_WithoutToken_Returns401()
    {
        using var client = _apiClient.Create();

        var response = await client.GetAsync(_apiClient.GetUrl($"drink/{_apiClient.ValidDrinkId}"));

        response.StatusCode.Should().Be(HttpStatusCode.Unauthorized);
    }
}
