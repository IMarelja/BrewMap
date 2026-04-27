using System.Net;
using System.Net.Http.Json;
using Microsoft.AspNetCore.WebUtilities;
using BrewMapEndpointUnitTest.Infrastructure;
using BrewMapEndpointUnitTest.ViewModels.Location;
using FluentAssertions;
using Xunit;

namespace BrewMapEndpointUnitTest.Tests;

public class LocationControllerTests
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
        response.StatusCode.Should().Be(HttpStatusCode.OK);

    }
}
