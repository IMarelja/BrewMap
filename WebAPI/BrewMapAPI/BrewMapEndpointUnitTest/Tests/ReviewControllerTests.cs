using System.Net;
using System.Net.Http.Json;
using BrewMapEndpointUnitTest.Infrastructure;
using BrewMapEndpointUnitTest.ViewModels.Drinks;
using BrewMapEndpointUnitTest.ViewModels.Location;
using BrewMapEndpointUnitTest.ViewModels.Review;
using FluentAssertions;
using Xunit;

namespace BrewMapEndpointUnitTest.Tests;

public class ReviewControllerTests(ITestOutputHelper output)
{
    private readonly ApiClient _apiClient = new();

    [Fact]
    public async Task CreateLocationReview_5Stars_UpdatesAggregatedScore()
    {
        var locationId = _apiClient.ValidLocationId;
        locationId.Should().NotBeNullOrEmpty("ValidLocationId must be set in appsettings.json");

        var token = await _apiClient.GetUserTokenAsync();
        using var client = _apiClient.CreateAuthenticated(token);

        // 1. Get current aggregated score
        var beforeResponse = await client.GetAsync(_apiClient.GetUrl($"Locations/{locationId}"));
        if (beforeResponse.StatusCode != HttpStatusCode.OK)
            Assert.Skip($"Could not fetch location {locationId}: {beforeResponse.StatusCode}");
        var before = await beforeResponse.Content.ReadFromJsonAsync<ReadLocationViewModel>(ApiClient.GetJsonOptions());
        if (before is null)
            Assert.Skip("Location response could not be deserialized");

        // 2. Calculate expected result after adding a 5-star review
        var expectedCount = before.TotalReviews + 1;
        var expectedAverage = (before.AverageRating * before.TotalReviews + 5.0) / expectedCount;

        // 3. Post a 5-star review for the location
        var createResponse = await client.PostAsJsonAsync(
            _apiClient.GetUrl($"Review/location/{locationId}"),
            new CreateReviewBodyViewModel { Rating = 5, Comment = "Integration test review" },
            ApiClient.GetJsonOptions());
        createResponse.StatusCode.Should().Be(HttpStatusCode.Created);
        var created = await createResponse.Content.ReadFromJsonAsync<ReadReviewViewModel>(ApiClient.GetJsonOptions());

        created.Should().NotBeNull();
        created.Rating.Should().Be(5);
        created.TargetType.Should().Be("location");
        created.TargetId.Should().Be(locationId);

        // 4. Get the location again
        var afterResponse = await client.GetAsync(_apiClient.GetUrl($"Locations/{locationId}"));
        afterResponse.StatusCode.Should().Be(HttpStatusCode.OK);
        var after = await afterResponse.Content.ReadFromJsonAsync<ReadLocationViewModel>(ApiClient.GetJsonOptions());
        after.Should().NotBeNull();

        // 5. Verify aggregated score matches expectation
        after.TotalReviews.Should().Be(expectedCount);
        after.AverageRating.Should().BeApproximately(expectedAverage, 0.001);
    }

    [Fact]
    public async Task CreateDrinkReview_5Stars_UpdatesAggregatedScore()
    {
        var drinkId = _apiClient.ValidDrinkId;
        drinkId.Should().NotBeNullOrEmpty("ValidDrinkId must be set in appsettings.json");

        var token = await _apiClient.GetUserTokenAsync();
        using var client = _apiClient.CreateAuthenticated(token);

        // 1. Get current aggregated score
        var beforeResponse = await client.GetAsync(_apiClient.GetUrl($"Drink/{drinkId}"));
        if (beforeResponse.StatusCode != HttpStatusCode.OK)
            Assert.Skip($"Could not fetch drink {drinkId}: {beforeResponse.StatusCode}");
        var before = await beforeResponse.Content.ReadFromJsonAsync<ReadDrinkViewModel>(ApiClient.GetJsonOptions());
        if (before is null)
            Assert.Skip("Drink response could not be deserialized");

        // 2. Calculate expected result after adding a 5-star review
        var expectedCount = before.AggregatedRating.Count + 1;
        var expectedAverage = (before.AggregatedRating.Average * before.AggregatedRating.Count + 5.0) / expectedCount;

        // 3. Post a 5-star review for the drink
        var createResponse = await client.PostAsJsonAsync(
            _apiClient.GetUrl($"Review/drink/{drinkId}"),
            new CreateReviewBodyViewModel { Rating = 5, Comment = "Integration test review" },
            ApiClient.GetJsonOptions());
        createResponse.StatusCode.Should().Be(HttpStatusCode.Created);
        var created = await createResponse.Content.ReadFromJsonAsync<ReadReviewViewModel>(ApiClient.GetJsonOptions());
        created.Should().NotBeNull();
        created.Rating.Should().Be(5);
        created.TargetType.Should().Be("product");
        created.TargetId.Should().Be(drinkId);

        // 4. Get the drink again
        var afterResponse = await client.GetAsync(_apiClient.GetUrl($"Drink/{drinkId}"));
        afterResponse.StatusCode.Should().Be(HttpStatusCode.OK);
        var after = await afterResponse.Content.ReadFromJsonAsync<ReadDrinkViewModel>(ApiClient.GetJsonOptions());
        after.Should().NotBeNull();

        // 5. Verify aggregated score matches expectation
        after.AggregatedRating.Count.Should().Be(expectedCount);
        after.AggregatedRating.Average.Should().BeApproximately(expectedAverage, 0.001);
    }
}
