using System.Net;
using System.Net.Http.Json;
using BrewMapEndpointUnitTest.Infrastructure;
using BrewMapEndpointUnitTest.ViewModels.Drinks;
using BrewMapEndpointUnitTest.ViewModels.Location;
using BrewMapEndpointUnitTest.ViewModels.Review;
using FluentAssertions;
using Xunit;

namespace BrewMapEndpointUnitTest.Tests;

// Review tested/non-tested endpoints
// ✅ Tested
// - PUT /api/Review/{id}
// - DELETE /api/Review/{id}
// - POST /api/Review/location/{locationId}
// - POST /api/Review/drink/{drinkId}
// - GET /api/Review/mine
// - GET /api/Review/{id}
// - GET /api/Review/location/{locationId}
// - GET /api/Review/drink/{drinkId}
// - GET /api/Review/byUser/{userId}
public class ReviewControllerTests(ITestOutputHelper output)
{
    private readonly ApiClient _apiClient = new();

    [Fact]
    public async Task GetReviewById_ReturnsReview_WhenReviewExists()
    {
        var token = string.Empty;
        try { token = await _apiClient.GetUserTokenAsync(); }
        catch { Assert.Skip("Could not fetch user token — is the API running and configured?"); }
        using var client = _apiClient.CreateAuthenticated(token);

        var ct = TestContext.Current.CancellationToken;

        // Fetch the user's own reviews to guarantee a valid review ID
        var mineResponse = await client.GetAsync(_apiClient.GetUrl("Review/mine"), ct);
        mineResponse.StatusCode.Should().Be(HttpStatusCode.OK);
        var myReviews = await mineResponse.Content.ReadFromJsonAsync<List<ReadReviewViewModel>>(ApiClient.GetJsonOptions(), ct);
        myReviews.Should().NotBeNull();

        if (myReviews.Count == 0)
            Assert.Skip("No reviews found for this user — cannot test GetById");

        var reviewId = myReviews.First().Id;

        var response = await client.GetAsync(_apiClient.GetUrl($"Review/{reviewId}"), ct);
        response.StatusCode.Should().Be(HttpStatusCode.OK);
        var review = await response.Content.ReadFromJsonAsync<ReadReviewViewModel>(ApiClient.GetJsonOptions(), ct);

        review.Should().NotBeNull();
        review.Id.Should().Be(reviewId);
        review.Rating.Should().BeInRange(1, 5);
    }

    [Fact]
    public async Task GetReviewsByLocation_ReturnsReviews_WhenLocationHasReviews()
    {
        if (string.IsNullOrEmpty(_apiClient.ValidLocationId))
            Assert.Skip("ValidLocationId is not configured in appsettings.json");

        var token = string.Empty;
        try { token = await _apiClient.GetUserTokenAsync(); }
        catch { Assert.Skip("Could not fetch user token — is the API running and configured?"); }
        using var client = _apiClient.CreateAuthenticated(token);

        var ct = TestContext.Current.CancellationToken;

        var response = await client.GetAsync(_apiClient.GetUrl($"Review/location/{_apiClient.ValidLocationId}"), ct);
        response.StatusCode.Should().Be(HttpStatusCode.OK);
        var reviews = await response.Content.ReadFromJsonAsync<List<ReadReviewViewModel>>(ApiClient.GetJsonOptions(), ct);

        reviews.Should().NotBeNull();

        if (reviews.Count == 0)
            Assert.Skip($"Location {_apiClient.ValidLocationId} has no reviews — theory cannot be validated");

        reviews.Should().AllSatisfy(r =>
        {
            r.TargetType.Should().Be("location");
            r.TargetId.Should().Be(_apiClient.ValidLocationId);
        });
    }

    [Fact]
    public async Task GetReviewsByDrink_ReturnsReviews_WhenDrinkHasReviews()
    {
        if (string.IsNullOrEmpty(_apiClient.ValidDrinkId))
            Assert.Skip("ValidDrinkId is not configured in appsettings.json");

        var token = string.Empty;
        try { token = await _apiClient.GetUserTokenAsync(); }
        catch { Assert.Skip("Could not fetch user token — is the API running and configured?"); }
        using var client = _apiClient.CreateAuthenticated(token);

        var ct = TestContext.Current.CancellationToken;

        var response = await client.GetAsync(_apiClient.GetUrl($"Review/drink/{_apiClient.ValidDrinkId}"), ct);
        response.StatusCode.Should().Be(HttpStatusCode.OK);
        var reviews = await response.Content.ReadFromJsonAsync<List<ReadReviewViewModel>>(ApiClient.GetJsonOptions(), ct);

        reviews.Should().NotBeNull();

        if (reviews.Count == 0)
            Assert.Skip($"Drink {_apiClient.ValidDrinkId} has no reviews — theory cannot be validated");

        reviews.Should().AllSatisfy(r =>
        {
            r.TargetType.Should().Be("product");
            r.TargetId.Should().Be(_apiClient.ValidDrinkId);
        });
    }

    [Fact]
    public async Task GetReviewsByUser_ReturnsReviews_WhenUserHasReviews()
    {
        if (string.IsNullOrEmpty(_apiClient.ValidUserId))
            Assert.Skip("ValidUserId is not configured in appsettings.json");

        var token = string.Empty;
        try { token = await _apiClient.GetUserTokenAsync(); }
        catch { Assert.Skip("Could not fetch user token — is the API running and configured?"); }
        using var client = _apiClient.CreateAuthenticated(token);

        var ct = TestContext.Current.CancellationToken;

        var response = await client.GetAsync(_apiClient.GetUrl($"Review/byUser/{_apiClient.ValidUserId}"), ct);
        response.StatusCode.Should().Be(HttpStatusCode.OK);
        var reviews = await response.Content.ReadFromJsonAsync<List<ReadReviewViewModel>>(ApiClient.GetJsonOptions(), ct);

        reviews.Should().NotBeNull();

        if (reviews.Count == 0)
            Assert.Skip($"User {_apiClient.ValidUserId} has no reviews — theory cannot be validated");

        reviews.Should().AllSatisfy(r => r.UserId.Should().Be(_apiClient.ValidUserId));
    }

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
        var before = await beforeResponse.Content.ReadFromJsonAsync<ReadLocationViewModel>(ApiClient.GetJsonOptions(), cancellationToken: TestContext.Current.CancellationToken);
        if (before is null)
            Assert.Skip("Location response could not be deserialized");

        // 2. Calculate expected result after adding a 5-star review
        var expectedCount = before.TotalReviews + 1;
        var expectedAverage = Math.Round((before.AverageRating * before.TotalReviews + 5.0) / expectedCount, 2);

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
        var before = await beforeResponse.Content.ReadFromJsonAsync<ReadDrinkViewModel>(ApiClient.GetJsonOptions(), cancellationToken: TestContext.Current.CancellationToken);
        if (before is null)
            Assert.Skip("Drink response could not be deserialized");

        // 2. Calculate expected result after adding a 5-star review
        var expectedCount = before.AggregatedRating.Count + 1;
        var expectedAverage = Math.Round((before.AggregatedRating.Average * before.AggregatedRating.Count + 5.0) / expectedCount, 2);

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

    [Fact]
    public async Task DeleteOwnReview_UpdatesAggregatedScoreAsExpected()
    {
        var token = string.Empty;
        try { token = await _apiClient.GetUserTokenAsync(); }
        catch { Assert.Skip("Could not fetch user token — is the API running and configured?"); }
        using var client = _apiClient.CreateAuthenticated(token);

        var ct = TestContext.Current.CancellationToken;

        var mineResponse = await client.GetAsync(_apiClient.GetUrl("Review/mine"), ct);
        mineResponse.StatusCode.Should().Be(HttpStatusCode.OK);
        var myReviews = await mineResponse.Content.ReadFromJsonAsync<List<ReadReviewViewModel>>(ApiClient.GetJsonOptions(), ct);
        myReviews.Should().NotBeNull();

        if (myReviews!.Count == 0)
            Assert.Skip("No reviews found for this user — nothing to delete");

        var reviewToDelete = myReviews.First();
        var (beforeAverage, beforeCount) = await GetTargetAggregate(client, reviewToDelete.TargetType, reviewToDelete.TargetId, ct);

        if (beforeCount <= 0)
            Assert.Skip("Target has no reviews in aggregate, cannot validate delete calculation.");

        var expectedCount = beforeCount - 1;
        var expectedAverage = expectedCount == 0
            ? 0
            : (beforeAverage * beforeCount - reviewToDelete.Rating) / expectedCount;

        var deleteResponse = await client.DeleteAsync(_apiClient.GetUrl($"Review/{reviewToDelete.Id}"), ct);
        deleteResponse.StatusCode.Should().Be(HttpStatusCode.NoContent);

        var (afterAverage, afterCount) = await GetTargetAggregate(client, reviewToDelete.TargetType, reviewToDelete.TargetId, ct);

        afterCount.Should().Be(expectedCount);
        afterAverage.Should().BeApproximately(expectedAverage, 0.001);
    }

    [Fact]
    public async Task AdminCanDeleteReviewCreatedByValidUser()
    {
        if (string.IsNullOrWhiteSpace(_apiClient.ValidLocationId))
            Assert.Skip("ValidLocationId is not configured in appsettings.json");

        var userToken = string.Empty;
        try { userToken = await _apiClient.GetUserTokenAsync(); }
        catch { Assert.Skip("Could not fetch user token — is the API running and configured?"); }
        using var userClient = _apiClient.CreateAuthenticated(userToken);

        var ct = TestContext.Current.CancellationToken;

        var createResponse = await userClient.PostAsJsonAsync(
            _apiClient.GetUrl($"Review/location/{_apiClient.ValidLocationId}"),
            new CreateReviewBodyViewModel { Rating = 5, Comment = "Delete by admin test" },
            ApiClient.GetJsonOptions(),
            ct);

        if (createResponse.StatusCode != HttpStatusCode.Created)
            Assert.Skip($"Could not create review for valid location {_apiClient.ValidLocationId}: {createResponse.StatusCode}");

        var createdReview = await createResponse.Content.ReadFromJsonAsync<ReadReviewViewModel>(ApiClient.GetJsonOptions(), ct);
        if (createdReview is null)
            Assert.Skip("Created review could not be deserialized");

        var (beforeDeleteAverage, beforeDeleteCount) = await GetTargetAggregate(
            userClient,
            createdReview.TargetType,
            createdReview.TargetId,
            ct);

        var expectedCount = beforeDeleteCount - 1;
        var expectedAverage = expectedCount == 0
            ? 0
            : (beforeDeleteAverage * beforeDeleteCount - createdReview.Rating) / expectedCount;

        var adminToken = string.Empty;
        try { adminToken = await _apiClient.GetAdminTokenAsync(); }
        catch { Assert.Skip("Could not fetch admin token — is the API running and configured?"); }
        using var adminClient = _apiClient.CreateAuthenticated(adminToken);

        var deleteResponse = await adminClient.DeleteAsync(_apiClient.GetUrl($"Review/{createdReview.Id}"), ct);
        deleteResponse.StatusCode.Should().Be(HttpStatusCode.NoContent);

        var (afterDeleteAverage, afterDeleteCount) = await GetTargetAggregate(
            adminClient,
            createdReview.TargetType,
            createdReview.TargetId,
            ct);

        afterDeleteCount.Should().Be(expectedCount);
        afterDeleteAverage.Should().BeApproximately(expectedAverage, 0.001);
    }

    private async Task<(double average, int count)> GetTargetAggregate(
        HttpClient client,
        string targetType,
        string targetId,
        CancellationToken ct)
    {
        if (targetType == "location")
        {
            var response = await client.GetAsync(_apiClient.GetUrl($"Locations/{targetId}"), ct);
            if (response.StatusCode != HttpStatusCode.OK)
                Assert.Skip($"Could not fetch location {targetId}: {response.StatusCode}");

            var location = await response.Content.ReadFromJsonAsync<ReadLocationViewModel>(ApiClient.GetJsonOptions(), ct);
            if (location is null)
                Assert.Skip("Location response could not be deserialized");

            return (location.AverageRating, location.TotalReviews);
        }

        if (targetType == "product")
        {
            var response = await client.GetAsync(_apiClient.GetUrl($"Drink/{targetId}"), ct);
            if (response.StatusCode != HttpStatusCode.OK)
                Assert.Skip($"Could not fetch drink {targetId}: {response.StatusCode}");

            var drink = await response.Content.ReadFromJsonAsync<ReadDrinkViewModel>(ApiClient.GetJsonOptions(), ct);
            if (drink is null)
                Assert.Skip("Drink response could not be deserialized");

            return (drink.AggregatedRating.Average, drink.AggregatedRating.Count);
        }

        Assert.Skip($"Unsupported review target type: {targetType}");
        return (0, 0);
    }
}
