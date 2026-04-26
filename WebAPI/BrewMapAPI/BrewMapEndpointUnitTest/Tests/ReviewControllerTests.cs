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
        if (string.IsNullOrEmpty(_apiClient.ValidLocationId))
            Assert.Skip("ValidLocationId is not configured in appsettings.json");

        var token = string.Empty;
        try { token = await _apiClient.GetUserTokenAsync(); }
        catch { Assert.Skip("Could not fetch user token — is the API running and configured?"); }
        using var client = _apiClient.CreateAuthenticated(token);

        var ct = TestContext.Current.CancellationToken;

        // 1. Get current aggregated score
        var beforeResponse = await client.GetAsync(_apiClient.GetUrl($"Locations/{_apiClient.ValidLocationId}"), ct);
        if (beforeResponse.StatusCode != HttpStatusCode.OK)
            Assert.Skip($"Could not fetch location {_apiClient.ValidLocationId}: {beforeResponse.StatusCode}");
        var before = await beforeResponse.Content.ReadFromJsonAsync<ReadLocationViewModel>(ApiClient.GetJsonOptions());
        if (before is null)
            Assert.Skip("Location response could not be deserialized");

        // 2. Calculate expected result after adding a 5-star review
        var expectedCount = before.TotalReviews + 1;
        var expectedAverage = (before.AverageRating * before.TotalReviews + 5.0) / expectedCount;

        // 3. Post a 5-star review for the location
        var createResponse = await client.PostAsJsonAsync(
            _apiClient.GetUrl($"Review/location/{_apiClient.ValidLocationId}"),
            new CreateReviewBodyViewModel { Rating = 5, Comment = "Integration test review" },
            ApiClient.GetJsonOptions(),
            ct);
        createResponse.StatusCode.Should().Be(HttpStatusCode.Created);
        var created = await createResponse.Content.ReadFromJsonAsync<ReadReviewViewModel>(ApiClient.GetJsonOptions(), ct);

        created.Should().NotBeNull();
        created.Rating.Should().Be(5);
        created.TargetType.Should().Be("location");
        created.TargetId.Should().Be(_apiClient.ValidLocationId);

        // 4. Get the location again
        var afterResponse = await client.GetAsync(_apiClient.GetUrl($"Locations/{_apiClient.ValidLocationId}"), ct);
        afterResponse.StatusCode.Should().Be(HttpStatusCode.OK);
        var after = await afterResponse.Content.ReadFromJsonAsync<ReadLocationViewModel>(ApiClient.GetJsonOptions(), ct);
        after.Should().NotBeNull();

        // 5. Verify aggregated score matches expectation
        var expectedStr = $"{expectedAverage:F2} ({expectedCount} count)";
        var actualStr   = $"{after.AverageRating:F2} ({after.TotalReviews} count)";
        var passed = after.TotalReviews == expectedCount && Math.Abs(after.AverageRating - expectedAverage) < 0.001;
        output.WriteLine(passed
            ? $"Success: expected score is {expectedStr} and the location's is {actualStr}"
            : $"Failure: expected score is {expectedStr} and the location's is {actualStr}");

        after.TotalReviews.Should().Be(expectedCount);
        after.AverageRating.Should().BeApproximately(expectedAverage, 0.001);
    }

    [Fact]
    public async Task CreateDrinkReview_5Stars_UpdatesAggregatedScore()
    {
        if (string.IsNullOrEmpty(_apiClient.ValidDrinkId))
            Assert.Skip("ValidDrinkId is not configured in appsettings.json");

        var token = string.Empty;
        try { token = await _apiClient.GetUserTokenAsync(); }
        catch { Assert.Skip("Could not fetch user token — is the API running and configured?"); }
        using var client = _apiClient.CreateAuthenticated(token);

        var ct = TestContext.Current.CancellationToken;

        // 1. Get current aggregated score
        var beforeResponse = await client.GetAsync(_apiClient.GetUrl($"Drink/{_apiClient.ValidDrinkId}"), ct);
        if (beforeResponse.StatusCode != HttpStatusCode.OK)
            Assert.Skip($"Could not fetch drink {_apiClient.ValidDrinkId}: {beforeResponse.StatusCode}");
        var before = await beforeResponse.Content.ReadFromJsonAsync<ReadDrinkViewModel>(ApiClient.GetJsonOptions());
        if (before is null)
            Assert.Skip("Drink response could not be deserialized");

        // 2. Calculate expected result after adding a 5-star review
        var expectedCount = before.AggregatedRating.Count + 1;
        var expectedAverage = (before.AggregatedRating.Average * before.AggregatedRating.Count + 5.0) / expectedCount;

        // 3. Post a 5-star review for the drink
        var createResponse = await client.PostAsJsonAsync(
            _apiClient.GetUrl($"Review/drink/{_apiClient.ValidDrinkId}"),
            new CreateReviewBodyViewModel { Rating = 5, Comment = "Integration test review" },
            ApiClient.GetJsonOptions(),
            ct);
        createResponse.StatusCode.Should().Be(HttpStatusCode.Created);
        var created = await createResponse.Content.ReadFromJsonAsync<ReadReviewViewModel>(ApiClient.GetJsonOptions(), ct);
        created.Should().NotBeNull();
        created.Rating.Should().Be(5);
        created.TargetType.Should().Be("product");
        created.TargetId.Should().Be(_apiClient.ValidDrinkId);

        // 4. Get the drink again
        var afterResponse = await client.GetAsync(_apiClient.GetUrl($"Drink/{_apiClient.ValidDrinkId}"), ct);
        afterResponse.StatusCode.Should().Be(HttpStatusCode.OK);
        var after = await afterResponse.Content.ReadFromJsonAsync<ReadDrinkViewModel>(ApiClient.GetJsonOptions(), ct);
        after.Should().NotBeNull();

        // 5. Verify aggregated score matches expectation
        var expectedStr = $"{expectedAverage:F2} ({expectedCount} count)";
        var actualStr   = $"{after.AggregatedRating.Average:F2} ({after.AggregatedRating.Count} count)";
        var passed = after.AggregatedRating.Count == expectedCount && Math.Abs(after.AggregatedRating.Average - expectedAverage) < 0.001;
        output.WriteLine(passed
            ? $"Success: expected score is {expectedStr} and the drink's is {actualStr}"
            : $"Failure: expected score is {expectedStr} and the drink's is {actualStr}");

        after.AggregatedRating.Count.Should().Be(expectedCount);
        after.AggregatedRating.Average.Should().BeApproximately(expectedAverage, 0.001);
    }

    [Fact]
    public async Task UpdateReview_To1StarNoComment_UpdatesAggregatedScoreAndClearsComment()
    {
        var token = string.Empty;
        try { token = await _apiClient.GetUserTokenAsync(); }
        catch { Assert.Skip("Could not fetch user token — is the API running and configured?"); }
        using var client = _apiClient.CreateAuthenticated(token);

        var ct = TestContext.Current.CancellationToken;

        // 1. Get the user's own reviews and pick the first one
        var mineResponse = await client.GetAsync(_apiClient.GetUrl("Review/mine"), ct);
        mineResponse.StatusCode.Should().Be(HttpStatusCode.OK);
        var myReviews = await mineResponse.Content.ReadFromJsonAsync<List<ReadReviewViewModel>>(ApiClient.GetJsonOptions(), ct);
        myReviews.Should().NotBeNull();

        if (myReviews.Count == 0)
            Assert.Skip("No reviews found for this user — nothing to update");

        var review = myReviews.First();

        // 2. Fetch the target's current aggregated score
        double currentAverage;
        int currentCount;

        if (review.TargetType == "location")
        {
            var res = await client.GetAsync(_apiClient.GetUrl($"Locations/{review.TargetId}"), ct);
            if (res.StatusCode != HttpStatusCode.OK)
                Assert.Skip($"Could not fetch location {review.TargetId}: {res.StatusCode}");
            var loc = await res.Content.ReadFromJsonAsync<ReadLocationViewModel>(ApiClient.GetJsonOptions(), ct);
            if (loc is null) Assert.Skip("Location response could not be deserialized");
            currentAverage = loc.AverageRating;
            currentCount   = loc.TotalReviews;
        }
        else
        {
            var res = await client.GetAsync(_apiClient.GetUrl($"Drink/{review.TargetId}"), ct);
            if (res.StatusCode != HttpStatusCode.OK)
                Assert.Skip($"Could not fetch drink {review.TargetId}: {res.StatusCode}");
            var drink = await res.Content.ReadFromJsonAsync<ReadDrinkViewModel>(ApiClient.GetJsonOptions(), ct);
            if (drink is null) Assert.Skip("Drink response could not be deserialized");
            currentAverage = drink.AggregatedRating.Average;
            currentCount   = drink.AggregatedRating.Count;
        }

        // 3. Calculate expected aggregate after swapping this review's rating to 1
        //    Count stays the same; only the sum changes by (1 - oldRating)
        var expectedAverage = (currentAverage * currentCount - review.Rating + 1.0) / currentCount;

        // 4. Update the review: rating = 1, clear the comment
        var updateResponse = await client.PutAsJsonAsync(
            _apiClient.GetUrl($"Review/{review.Id}"),
            new UpdateReviewViewModel { Rating = 1, Comment = "" },
            ApiClient.GetJsonOptions(),
            ct);
        updateResponse.StatusCode.Should().Be(HttpStatusCode.OK);
        var updated = await updateResponse.Content.ReadFromJsonAsync<ReadReviewViewModel>(ApiClient.GetJsonOptions(), ct);

        // 5. Verify the review itself
        updated.Should().NotBeNull();
        updated.Rating.Should().Be(1);
        updated.Comment.Should().BeNullOrEmpty();

        // 6. Fetch the target again and verify the aggregated score
        double newAverage;
        int newCount;

        if (review.TargetType == "location")
        {
            var res = await client.GetAsync(_apiClient.GetUrl($"Locations/{review.TargetId}"), ct);
            res.StatusCode.Should().Be(HttpStatusCode.OK);
            var after = await res.Content.ReadFromJsonAsync<ReadLocationViewModel>(ApiClient.GetJsonOptions(), ct);
            after.Should().NotBeNull();
            newAverage = after.AverageRating;
            newCount   = after.TotalReviews;
        }
        else
        {
            var res = await client.GetAsync(_apiClient.GetUrl($"Drink/{review.TargetId}"), ct);
            res.StatusCode.Should().Be(HttpStatusCode.OK);
            var after = await res.Content.ReadFromJsonAsync<ReadDrinkViewModel>(ApiClient.GetJsonOptions(), ct);
            after.Should().NotBeNull();
            newAverage = after.AggregatedRating.Average;
            newCount   = after.AggregatedRating.Count;
        }

        newCount.Should().Be(currentCount);
        newAverage.Should().BeApproximately(expectedAverage, 0.001);
    }
}
