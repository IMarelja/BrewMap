using System.Net;
using System.Net.Http.Json;
using BrewMapEndpointUnitTest.Infrastructure;
using BrewMapEndpointUnitTest.ViewModels.Drinks;
using FluentAssertions;
using Xunit;

namespace BrewMapEndpointUnitTest.Tests;

public class DrinkControllerTests
{
    private readonly ApiClient _apiClient = new();

    [Fact]
    public async Task GetById_ValidIdWithToken_Returns200WithDrink()
    {
        var token = await _apiClient.GetAdminTokenAsync();
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
        var token = await _apiClient.GetAdminTokenAsync();
        using var client = _apiClient.CreateAuthenticated(token);

        var invalid_id = "invalid_id";

        var response = await client.GetAsync(_apiClient.GetUrl($"Drink/{invalid_id}"));

        response.StatusCode.Should().Be(HttpStatusCode.BadRequest);
    }
}
