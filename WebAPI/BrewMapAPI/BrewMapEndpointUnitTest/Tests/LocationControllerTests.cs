using System.Globalization;
using System.Net;
using System.Net.Http.Json;
using Microsoft.AspNetCore.WebUtilities;
using BrewMapEndpointUnitTest.Infrastructure;
using BrewMapEndpointUnitTest.ViewModels.Location;
using FluentAssertions;
using Xunit;

namespace BrewMapEndpointUnitTest.Tests;

// Locations tested/non-tested endpoints
// ✅ Tested
// - GET /api/Locations/search
// - GET /api/Locations/pins
// 🚫 Not tested
// - POST /api/Locations
// - GET /api/Locations/{id}
// - PUT /api/Locations/{id}
// - DELETE /api/Locations/{id}
public class LocationControllerTests(ITestOutputHelper output)
{
    private readonly ApiClient _apiClient = new();

    [Fact]
    public async Task GetLocationSearch_Returns200WithLocations()
    {
        if (!_apiClient.HasSearchLongitude || !_apiClient.HasSearchLatitude || !_apiClient.HasSearchRadiusMeters)
            Assert.Skip("SearchLongitude, SearchLatitude, or SearchRadiusMeters is missing/empty in appsettings.json");

        var token = string.Empty;

        try{ token = await _apiClient.GetUserTokenAsync(); }
        catch{ Assert.Skip("Could not fetch user token — is the API running and configured?"); }

        using var client = _apiClient.CreateAuthenticated(token);

        var queryParams = new Dictionary<string, string?>
        {
            //["query"] = "coffee",
            //["minRating"] = "4.5",
            //["drinkQuery"] = "latte",
            ["longitude"] = _apiClient.SearchLongitude.ToString(),
            ["latitude"] = _apiClient.SearchLatitude.ToString(),
            ["radiusMeters"] = _apiClient.SearchRadiusMeters.ToString()
        };

        var url = QueryHelpers.AddQueryString("Locations/search", queryParams);

        //url = QueryHelpers.AddQueryString(url, "categoryTags", "cafe");
        //url = QueryHelpers.AddQueryString(url, "categoryTags", "bakery");
        //url = QueryHelpers.AddQueryString(url, "paymentOptionTags", "card");
        //url = QueryHelpers.AddQueryString(url, "paymentOptionTags", "cash");

        var response = await client.GetAsync(
            _apiClient.GetUrl(url),
            TestContext.Current.CancellationToken
        );

        var body = await response.Content.ReadFromJsonAsync<List<ReadLocationViewModel>>(ApiClient.GetJsonOptions()
, cancellationToken: TestContext.Current.CancellationToken);

        response.StatusCode.Should().Be(HttpStatusCode.OK);
        body.Should().NotBeNull();
        body.Should().NotBeEmpty();

    }

    [Fact]
    public async Task GetPins_LargestRangeThenShrink_ReturnsFewer()
    {
        var token = string.Empty;
        try { token = await _apiClient.GetUserTokenAsync(); }
        catch { Assert.Skip("Could not fetch user token — is the API running and configured?"); }
        using var client = _apiClient.CreateAuthenticated(token);

        var ct = TestContext.Current.CancellationToken;

        // 1. World-spanning bounding box — the maximum possible range
        var worldParams = new Dictionary<string, string?>
        {
            ["minLon"] = "-180",
            ["maxLon"] = "180",
            ["minLat"] = "-90",
            ["maxLat"] = "90"
        };
        var worldResponse = await client.GetAsync(
            _apiClient.GetUrl(QueryHelpers.AddQueryString("Locations/pins", worldParams)), ct);
        worldResponse.StatusCode.Should().Be(HttpStatusCode.OK);
        var allPins = await worldResponse.Content.ReadFromJsonAsync<List<ReadPinViewModel>>(ApiClient.GetJsonOptions(), ct);
        allPins.Should().NotBeNull();

        if (allPins!.Count == 0)
            Assert.Skip("No pins found in the database — cannot validate range shrinking");

        output.WriteLine($"World range returned {allPins.Count} pin(s)");

        // 2. Narrow bounding box centred on the configured search coordinates (≈1 km radius)
        const double delta = 0.01;
        var narrowParams = new Dictionary<string, string?>
        {
            ["minLon"] = (_apiClient.SearchLongitude - delta).ToString(CultureInfo.InvariantCulture),
            ["maxLon"] = (_apiClient.SearchLongitude + delta).ToString(CultureInfo.InvariantCulture),
            ["minLat"] = (_apiClient.SearchLatitude - delta).ToString(CultureInfo.InvariantCulture),
            ["maxLat"] = (_apiClient.SearchLatitude + delta).ToString(CultureInfo.InvariantCulture)
        };
        var narrowResponse = await client.GetAsync(
            _apiClient.GetUrl(QueryHelpers.AddQueryString("Locations/pins", narrowParams)), ct);
        narrowResponse.StatusCode.Should().Be(HttpStatusCode.OK);
        var narrowPins = await narrowResponse.Content.ReadFromJsonAsync<List<ReadPinViewModel>>(ApiClient.GetJsonOptions(), ct);
        narrowPins.Should().NotBeNull();

        output.WriteLine($"Narrow range returned {narrowPins!.Count} pin(s)");

        if (narrowPins.Count == allPins.Count)
            Assert.Skip("All pins lie within the narrow bounding box — cannot validate that shrinking reduces count");

        narrowPins.Count.Should().BeLessThan(allPins.Count);
    }
}
