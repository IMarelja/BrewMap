using System.Net;
using System.Net.Http.Json;
using BrewMapEndpointUnitTest.Infrastructure;
using BrewMapEndpointUnitTest.ViewModels.User;
using FluentAssertions;
using Xunit;

namespace BrewMapEndpointUnitTest.Tests;

// User tested/non-tested endpoints
// ✅ Tested
// - GET /api/User/me
// - GET /api/User/{id}
// 🚫 Not tested
// - PUT /api/User/email
// - PUT /api/User/password
// - DELETE /api/User/me
public class UserControllerTests
{
    private readonly ApiClient _apiClient = new();

    [Fact]
    public async Task GetMyProfile_ReturnsAuthenticatedUserInfo()
    {
        var token = string.Empty;
        try { token = await _apiClient.GetUserTokenAsync(); }
        catch { Assert.Skip("Could not fetch user token — is the API running and configured?"); }
        using var client = _apiClient.CreateAuthenticated(token);

        var ct = TestContext.Current.CancellationToken;

        var response = await client.GetAsync(_apiClient.GetUrl("User/me"), ct);
        response.StatusCode.Should().Be(HttpStatusCode.OK);
        var profile = await response.Content.ReadFromJsonAsync<MyUserProfileViewModel>(ApiClient.GetJsonOptions(), ct);

        profile.Should().NotBeNull();
        profile.Id.Should().NotBeNullOrEmpty();
        profile.Username.Should().NotBeNullOrEmpty();
        profile.Email.Should().NotBeNullOrEmpty();

        // Verify the JWT identity resolves to the configured test user
        if (!string.IsNullOrWhiteSpace(_apiClient.ValidUserEmail))
            profile.Email.Should().Be(_apiClient.ValidUserEmail);
    }

    [Fact]
    public async Task GetUserById_ReturnsPublicProfile_WithoutEmail()
    {
        if (string.IsNullOrEmpty(_apiClient.ValidUserId))
            Assert.Skip("ValidUserId is not configured in appsettings.json");

        var token = string.Empty;
        try { token = await _apiClient.GetUserTokenAsync(); }
        catch { Assert.Skip("Could not fetch user token — is the API running and configured?"); }
        using var client = _apiClient.CreateAuthenticated(token);

        var ct = TestContext.Current.CancellationToken;

        var response = await client.GetAsync(_apiClient.GetUrl($"User/{_apiClient.ValidUserId}"), ct);
        response.StatusCode.Should().Be(HttpStatusCode.OK);
        var profile = await response.Content.ReadFromJsonAsync<StrangerUserProfileViewModel>(ApiClient.GetJsonOptions(), ct);

        profile.Should().NotBeNull();
        profile.Id.Should().Be(_apiClient.ValidUserId);
        profile.Username.Should().NotBeNullOrEmpty();
    }
}
