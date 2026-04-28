using System.Net;
using System.Net.Http.Json;
using BrewMapEndpointUnitTest.Infrastructure;
using BrewMapEndpointUnitTest.ViewModels.Drinks;
using BrewMapEndpointUnitTest.ViewModels.Location;
using FluentAssertions;
using Xunit;

namespace BrewMapEndpointUnitTest.Tests;

// Drink tested/non-tested endpoints
// ✅ Tested
// - GET /api/Drink/{id}
// - GET /api/Drink/location/{locationId}
// - GET /api/Drink/location/{locationId}/best-drink
// 🚫 Not tested
// - PUT /api/Drink/{id}
// - DELETE /api/Drink/{id}
// - POST /api/Drink
public class DrinkControllerTests
{
    private readonly ApiClient _apiClient = new();

    [Fact]
    public async Task GetById_ValidIdWithToken_Returns200WithDrink()
    {
        if (string.IsNullOrEmpty(_apiClient.ValidDrinkId))
            Assert.Skip("ValidDrinkId is not configured in appsettings.json");

        var token = string.Empty;
        try { token = await _apiClient.GetAdminTokenAsync(); }
        catch { Assert.Skip("Could not fetch admin token — is the API running and configured?"); }
        using var client = _apiClient.CreateAuthenticated(token);

        var response = await client.GetAsync(_apiClient.GetUrl($"Drink/{_apiClient.ValidDrinkId}"), TestContext.Current.CancellationToken);
        var body = await response.Content.ReadFromJsonAsync<ReadDrinkViewModel>(ApiClient.GetJsonOptions(), cancellationToken: TestContext.Current.CancellationToken);

        response.StatusCode.Should().Be(HttpStatusCode.OK);
        body.Should().NotBeNull();
        body.Id.Should().Be(_apiClient.ValidDrinkId);
        body.Name.Should().NotBeNullOrEmpty();
    }

    [Fact]
    public async Task GetById_InvalidId_Returns400()
    {
        var token = string.Empty;
        try { token = await _apiClient.GetAdminTokenAsync(); }
        catch { Assert.Skip("Could not fetch admin token — is the API running and configured?"); }
        using var client = _apiClient.CreateAuthenticated(token);

        var tag = "tag_invalid";

        var response = await client.GetAsync(_apiClient.GetUrl($"Drink/{tag}"), TestContext.Current.CancellationToken);

        response.StatusCode.Should().Be(HttpStatusCode.BadRequest);
    }

    [Fact]
    public async Task GetBestDrink_MatchesLocallyComputedBest()
    {
        // 1. Standard precondition checks
        if (string.IsNullOrEmpty(_apiClient.ValidLocationId))
            Assert.Skip("ValidLocationId is not configured in appsettings.json");

        var token = string.Empty;
        try { token = await _apiClient.GetUserTokenAsync(); }
        catch { Assert.Skip("Could not fetch user token — is the API running and configured?"); }
        using var client = _apiClient.CreateAuthenticated(token);

        // 2. Fetch all drinks at the location
        var allDrinksResponse = await client.GetAsync(
            _apiClient.GetUrl($"Drink/location/{_apiClient.ValidLocationId}"),
            TestContext.Current.CancellationToken);

        if (allDrinksResponse.StatusCode == HttpStatusCode.NotFound)
            Assert.Skip("Location was not found — skipping best-drink test.");

        var allDrinks = await allDrinksResponse.Content
            .ReadFromJsonAsync<List<ReadDrinkViewModel>>(ApiClient.GetJsonOptions(),
                cancellationToken: TestContext.Current.CancellationToken);

        if (allDrinks == null || allDrinks.Count == 0)
            Assert.Skip("Location has no drinks — skipping best-drink test.");

        // 3. Compute the expected best drink locally
        var expected = allDrinks
            .OrderByDescending(d => d.AggregatedRating.Average)
            .First();

        // 4. Fetch the best drink from the controller
        var bestResponse = await client.GetAsync(
            _apiClient.GetUrl($"Drink/location/{_apiClient.ValidLocationId}/best-drink"),
            TestContext.Current.CancellationToken);

        bestResponse.StatusCode.Should().Be(HttpStatusCode.OK);

        var actual = await bestResponse.Content
            .ReadFromJsonAsync<ReadBestDrinkViewModel>(ApiClient.GetJsonOptions(),
                cancellationToken: TestContext.Current.CancellationToken);

        actual.Should().NotBeNull();

        // 5. Compare — report each mismatch individually
        var failures = new List<string>();
        if (actual.Id != expected.Id)
            failures.Add($"Id: expected '{expected.Id}', got '{actual.Id}'");
        if (actual.Name != expected.Name)
            failures.Add($"Name: expected '{expected.Name}', got '{actual.Name}'");
        if (actual.Rating != expected.AggregatedRating.Average)
            failures.Add($"Rating: expected {expected.AggregatedRating.Average}, got {actual.Rating}");

        failures.Should().BeEmpty(because: string.Join("; ", failures));
    }

    [Fact]
    public async Task GetDrinksBy_ValidLocationId_Returns200WithDrinks()
    {
        if (string.IsNullOrEmpty(_apiClient.ValidLocationId))
            Assert.Skip("ValidLocationId is not configured in appsettings.json");

        var token = string.Empty;
        try { token = await _apiClient.GetAdminTokenAsync(); }
        catch { Assert.Skip("Could not fetch admin token — is the API running and configured?"); }
        using var client = _apiClient.CreateAuthenticated(token);

        var response = await client.GetAsync(_apiClient.GetUrl($"Drink/location/{_apiClient.ValidLocationId}"), TestContext.Current.CancellationToken);
        var body = await response.Content.ReadFromJsonAsync<List<ReadLocationViewModel>>(ApiClient.GetJsonOptions(), cancellationToken: TestContext.Current.CancellationToken);
    
        response.StatusCode.Should().Be(HttpStatusCode.OK);
        body.Should().NotBeNull();
        body.Should().NotBeEmpty();
    }
}
