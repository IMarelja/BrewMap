using System.Globalization;
using System.Net;
using System.Net.Http.Json;
using Microsoft.AspNetCore.WebUtilities;
using BrewMapEndpointUnitTest.Infrastructure;
using BrewMapEndpointUnitTest.ViewModels.Location;
using static BrewMapEndpointUnitTest.Infrastructure.GeoConvert;
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
    public async Task GetPins_20KmRangeThenShrinkTo100m_ReturnsFewer()
    {
        if (!_apiClient.HasSearchLongitude || !_apiClient.HasSearchLatitude)
            Assert.Skip("SearchLongitude or SearchLatitude is missing/empty in appsettings.json");

        var token = string.Empty;
        try { token = await _apiClient.GetUserTokenAsync(); }
        catch { Assert.Skip("Could not fetch user token — is the API running and configured?"); }
        using var client = _apiClient.CreateAuthenticated(token);

        var ct = TestContext.Current.CancellationToken;

        // 1. Large bounding box: ~20 km around the configured coordinates
        double largeDelta = KmToDegrees(20);
        var largeParams = new Dictionary<string, string?>
        {
            ["minLon"] = (_apiClient.SearchLongitude - largeDelta).ToString(CultureInfo.InvariantCulture),
            ["maxLon"] = (_apiClient.SearchLongitude + largeDelta).ToString(CultureInfo.InvariantCulture),
            ["minLat"] = (_apiClient.SearchLatitude - largeDelta).ToString(CultureInfo.InvariantCulture),
            ["maxLat"] = (_apiClient.SearchLatitude + largeDelta).ToString(CultureInfo.InvariantCulture)
        };
        var largeResponse = await client.GetAsync(
            _apiClient.GetUrl(QueryHelpers.AddQueryString("Locations/pins", largeParams)), ct);
        largeResponse.StatusCode.Should().Be(HttpStatusCode.OK);
        var largePins = await largeResponse.Content.ReadFromJsonAsync<List<ReadPinViewModel>>(ApiClient.GetJsonOptions(), ct);
        largePins.Should().NotBeNull();

        if (largePins.Count == 0)
            Assert.Skip("No pins found within 20 km of the configured coordinates — cannot validate range shrinking");

        output.WriteLine($"20 km range returned {largePins.Count} pin(s)");

        // 2. Small bounding box: ~100 m around the same centre
        double smallDelta = MetersToDegrees(100);
        var smallParams = new Dictionary<string, string?>
        {
            ["minLon"] = (_apiClient.SearchLongitude - smallDelta).ToString(CultureInfo.InvariantCulture),
            ["maxLon"] = (_apiClient.SearchLongitude + smallDelta).ToString(CultureInfo.InvariantCulture),
            ["minLat"] = (_apiClient.SearchLatitude - smallDelta).ToString(CultureInfo.InvariantCulture),
            ["maxLat"] = (_apiClient.SearchLatitude + smallDelta).ToString(CultureInfo.InvariantCulture)
        };
        var smallResponse = await client.GetAsync(
            _apiClient.GetUrl(QueryHelpers.AddQueryString("Locations/pins", smallParams)), ct);
        smallResponse.StatusCode.Should().Be(HttpStatusCode.OK);
        var smallPins = await smallResponse.Content.ReadFromJsonAsync<List<ReadPinViewModel>>(ApiClient.GetJsonOptions(), ct);
        smallPins.Should().NotBeNull();

        output.WriteLine($"100 m range returned {smallPins.Count} pin(s)");

        if (smallPins.Count == largePins.Count)
            Assert.Skip("All pins lie within the 100 m bounding box — cannot validate that shrinking reduces count");

        smallPins.Count.Should().BeLessThan(largePins.Count);
    }
}
