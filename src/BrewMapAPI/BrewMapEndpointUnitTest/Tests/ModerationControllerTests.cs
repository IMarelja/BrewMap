using System.Net;
using System.Net.Http.Json;
using BrewMapEndpointUnitTest.Infrastructure;
using BrewMapEndpointUnitTest.ViewModels.Moderation;
using FluentAssertions;
using Xunit;

namespace BrewMapEndpointUnitTest.Tests;

// Moderation tested/non-tested endpoints
// ✅ Tested
// - GET  /api/Moderation/user/{id}
// - PATCH /api/Moderation/user/{id}  (role: admin → user revert)
// - PATCH /api/Moderation/user/{id}  (suspended: true → false revert)
// 🚫 Not tested
// - DELETE /api/Moderation/user/{id}
public class ModerationControllerTests
{
    private readonly ApiClient _apiClient = new();

    [Fact]
    public async Task PatchUser_GrantAdminRole_SucceedsAndReverts()
    {
        if (string.IsNullOrEmpty(_apiClient.ValidAdminUsername) || string.IsNullOrEmpty(_apiClient.ValidAdminPassword))
            Assert.Skip("ValidAdmin credentials are not configured in appsettings.json");

        var token = string.Empty;
        try { token = await _apiClient.GetAdminTokenAsync(); }
        catch { Assert.Skip("Could not fetch admin token — is the API running and configured?"); }

        if (string.IsNullOrEmpty(_apiClient.ValidUserId))
            Assert.Skip("ValidUserId is not configured in appsettings.json");

        var ct = TestContext.Current.CancellationToken;
        using var client = _apiClient.CreateAuthenticated(token);
        var userUrl = _apiClient.GetUrl($"Moderation/user/{_apiClient.ValidUserId}");

        // Fetch current user state
        var beforeResponse = await client.GetAsync(userUrl, ct);
        beforeResponse.StatusCode.Should().Be(HttpStatusCode.OK);
        var before = await beforeResponse.Content.ReadFromJsonAsync<ModeratedUserInfoViewModel>(ApiClient.GetJsonOptions(), cancellationToken: ct);
        before.Should().NotBeNull();

        if (before!.Role == "admin")
            Assert.Skip($"User {before.Username} already has admin privileges — skipping to avoid state corruption.");

        // Grant admin
        var grantResponse = await client.PatchAsJsonAsync(
            userUrl,
            new UpdateUserModerationRequestViewModel { Role = "admin" },
            ApiClient.GetJsonOptions(),
            ct);
        grantResponse.StatusCode.Should().Be(HttpStatusCode.OK);
        var grantResult = await grantResponse.Content.ReadFromJsonAsync<ModerationResultViewModel>(ApiClient.GetJsonOptions(), cancellationToken: ct);
        grantResult.Should().NotBeNull();
        grantResult!.Success.Should().BeTrue();

        // Verify role changed
        var afterResponse = await client.GetAsync(userUrl, ct);
        afterResponse.StatusCode.Should().Be(HttpStatusCode.OK);
        var after = await afterResponse.Content.ReadFromJsonAsync<ModeratedUserInfoViewModel>(ApiClient.GetJsonOptions(), cancellationToken: ct);
        after.Should().NotBeNull();
        after!.Role.Should().Be("admin");

        // Revert to user
        var revertResponse = await client.PatchAsJsonAsync(
            userUrl,
            new UpdateUserModerationRequestViewModel { Role = "user" },
            ApiClient.GetJsonOptions(),
            ct);
        revertResponse.StatusCode.Should().Be(HttpStatusCode.OK);
    }

    [Fact]
    public async Task PatchUser_SuspendUser_SucceedsAndReverts()
    {
        if (string.IsNullOrEmpty(_apiClient.ValidAdminUsername) || string.IsNullOrEmpty(_apiClient.ValidAdminPassword))
            Assert.Skip("ValidAdmin credentials are not configured in appsettings.json");

        var token = string.Empty;
        try { token = await _apiClient.GetAdminTokenAsync(); }
        catch { Assert.Skip("Could not fetch admin token — is the API running and configured?"); }

        if (string.IsNullOrEmpty(_apiClient.ValidUserId))
            Assert.Skip("ValidUserId is not configured in appsettings.json");

        var ct = TestContext.Current.CancellationToken;
        using var client = _apiClient.CreateAuthenticated(token);
        var userUrl = _apiClient.GetUrl($"Moderation/user/{_apiClient.ValidUserId}");

        // Fetch current user state
        var beforeResponse = await client.GetAsync(userUrl, ct);
        beforeResponse.StatusCode.Should().Be(HttpStatusCode.OK);
        var before = await beforeResponse.Content.ReadFromJsonAsync<ModeratedUserInfoViewModel>(ApiClient.GetJsonOptions(), cancellationToken: ct);
        before.Should().NotBeNull();

        if (!before!.IsActive)
            Assert.Skip($"User {before.Username} is already suspended — skipping to avoid state corruption.");

        // Suspend user
        var suspendResponse = await client.PatchAsJsonAsync(
            userUrl,
            new UpdateUserModerationRequestViewModel { Suspended = true },
            ApiClient.GetJsonOptions(),
            ct);
        suspendResponse.StatusCode.Should().Be(HttpStatusCode.OK);
        var suspendResult = await suspendResponse.Content.ReadFromJsonAsync<ModerationResultViewModel>(ApiClient.GetJsonOptions(), cancellationToken: ct);
        suspendResult.Should().NotBeNull();
        suspendResult!.Success.Should().BeTrue();

        // Verify IsActive is now false
        var afterResponse = await client.GetAsync(userUrl, ct);
        afterResponse.StatusCode.Should().Be(HttpStatusCode.OK);
        var after = await afterResponse.Content.ReadFromJsonAsync<ModeratedUserInfoViewModel>(ApiClient.GetJsonOptions(), cancellationToken: ct);
        after.Should().NotBeNull();
        after!.IsActive.Should().BeFalse();

        // Revert — unsuspend
        var revertResponse = await client.PatchAsJsonAsync(
            userUrl,
            new UpdateUserModerationRequestViewModel { Suspended = false },
            ApiClient.GetJsonOptions(),
            ct);
        revertResponse.StatusCode.Should().Be(HttpStatusCode.OK);
    }
}
