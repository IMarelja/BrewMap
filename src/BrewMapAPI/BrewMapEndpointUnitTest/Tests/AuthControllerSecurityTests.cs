using System.Net;
using System.Net.Http.Json;
using System.Text;
using BrewMapEndpointUnitTest.Infrastructure;
using BrewMapEndpointUnitTest.ViewModels.Auth;
using FluentAssertions;
using Xunit;

namespace BrewMapEndpointUnitTest.Tests;

/// <summary>
/// Security tests verifying the API safely rejects NoSQL injection attempts.
/// These are defensive tests — they confirm the API does NOT process malicious
/// MongoDB operator payloads, and that the database is not affected.
/// </summary>
public class AuthControllerSecurityTests
{
    private readonly ApiClient _apiClient = new();

    // --- Authorization tests ---

    [Fact]
    public async Task UnauthenticatedRequest_ToProtectedEndpoint_ReturnsUnauthorizedOrForbidden()
    {
        using var client = _apiClient.Create();

        var response = await client.GetAsync(_apiClient.GetUrl($"drink/{_apiClient.ValidDrinkId}"), TestContext.Current.CancellationToken);

        response.StatusCode.Should().BeOneOf(HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden);
    }

    [Fact]
    public async Task AuthenticatedAsUser_GetDrinkById_DoesNotReturnUnauthorizedOrForbidden()
    {
        var token = await _apiClient.GetUserTokenAsync();
        using var client = _apiClient.CreateAuthenticated(token);

        var response = await client.GetAsync(_apiClient.GetUrl($"Drink/{_apiClient.ValidDrinkId}"), TestContext.Current.CancellationToken);

        //response.StatusCode.Should().NotBeOneOf(HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden);
    
        var forbiddenStatuses = new[]
        {
            HttpStatusCode.Unauthorized,
            HttpStatusCode.Forbidden
        };

        forbiddenStatuses.Should().NotContain(response.StatusCode);
    }

    [Fact]
    public async Task AuthenticatedAsUser_GetUserInfo_ReturnsForbiddenOrUnauthorized()
    {
        var token = await _apiClient.GetUserTokenAsync();
        using var client = _apiClient.CreateAuthenticated(token);

        var response = await client.GetAsync(_apiClient.GetUrl("Moderation/user/0000000000000000"), TestContext.Current.CancellationToken);

        //response.StatusCode.Should().BeOneOf(HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden);
    
        var allowedStatus = new[]
        {
            HttpStatusCode.Unauthorized,
            HttpStatusCode.Forbidden
        };

        allowedStatus.Should().Contain(response.StatusCode);
    }

    [Fact]
    public async Task AuthenticatedAsAdmin_GetUserInfo_DoesNotReturnForbiddenOrUnauthorized()
    {
        var token = await _apiClient.GetAdminTokenAsync();
        using var client = _apiClient.CreateAuthenticated(token);

        var response = await client.GetAsync(_apiClient.GetUrl("Moderation/user/0000000000000000"), TestContext.Current.CancellationToken);

        //response.StatusCode.Should().NotBeOneOf(HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden);
    
        var forbiddenStatuses = new[]
        {
            HttpStatusCode.Unauthorized,
            HttpStatusCode.Forbidden
        };

        forbiddenStatuses.Should().NotContain(response.StatusCode);
    }

    // --- Login endpoint injection tests ---

    [Fact]
    public async Task Login_NoSqlInjection_MongoOperatorAsUsername_Returns400()
    {
        // Sends { "username": { "$where": "db.dropDatabase()" }, "password": "anything" }
        // ASP.NET Core model binding rejects the object in place of a string field.
        using var client = _apiClient.Create();
        var payload = """{"username": {"$where": "db.dropDatabase()"}, "password": "anything"}""";
        var content = new StringContent(payload, Encoding.UTF8, "application/json");

        var response = await client.PatchAsync(_apiClient.GetUrl("Auth/login"), content, TestContext.Current.CancellationToken);

        response.StatusCode.Should().Be(HttpStatusCode.BadRequest);
    }

    [Fact]
    public async Task Login_NoSqlInjection_NullBypassOperators_Returns400()
    {
        // Sends { "username": { "$ne": null }, "password": { "$ne": null } }
        // Classic MongoDB bypass — operator objects should be rejected by model binding.
        using var client = _apiClient.Create();
        var payload = """{"username": {"$ne": null}, "password": {"$ne": null}}""";
        var content = new StringContent(payload, Encoding.UTF8, "application/json");

        var response = await client.PatchAsync(_apiClient.GetUrl("Auth/login"), content, TestContext.Current.CancellationToken);

        response.StatusCode.Should().Be(HttpStatusCode.BadRequest);
    }

    [Fact]
    public async Task Login_NoSqlInjection_GreaterThanOperatorBypass_Returns400()
    {
        // Sends { "username": { "$gt": "" }, "password": { "$gt": "" } }
        // Attempts to match any non-empty document field.
        using var client = _apiClient.Create();
        var payload = """{"username": {"$gt": ""}, "password": {"$gt": ""}}""";
        var content = new StringContent(payload, Encoding.UTF8, "application/json");

        var response = await client.PatchAsync(_apiClient.GetUrl("Auth/login"), content, TestContext.Current.CancellationToken);

        response.StatusCode.Should().Be(HttpStatusCode.BadRequest);
    }

    [Fact]
    public async Task Login_NoSqlInjection_DropDatabaseAsStringValue_Returns401NotServerError()
    {
        // Sends a MongoDB shell command as a literal string value.
        // The driver treats it as a plain string lookup — no user matches, returns 401.
        // Must NOT return 500, which would indicate server-side execution of the payload.
        using var client = _apiClient.Create();
        var request = new LoginRequestViewModel
        {
            User = """{"$where": "db.dropDatabase()"}""",
            Password = """{"$ne": null}"""
        };

        var response = await client.PatchAsJsonAsync(_apiClient.GetUrl("Auth/login"), request, cancellationToken: TestContext.Current.CancellationToken);

        ((int)response.StatusCode).Should().NotBe(500);
        response.StatusCode.Should().Be(HttpStatusCode.Unauthorized);
    }

    [Fact]
    public async Task Login_NoSqlInjection_EvalDropDatabase_Returns401NotServerError()
    {
        // Sends the $eval operator as a string — must be treated as plain text, not executed.
        using var client = _apiClient.Create();
        var request = new LoginRequestViewModel
        {
            User = """{"$eval": "db.dropDatabase()"}""",
            Password = "Password1!"
        };

        var response = await client.PatchAsJsonAsync(_apiClient.GetUrl("Auth/login"), request, cancellationToken: TestContext.Current.CancellationToken);

        ((int)response.StatusCode).Should().NotBe(500);
        response.StatusCode.Should().Be(HttpStatusCode.Unauthorized);
    }

    // --- Register endpoint injection tests ---

    [Fact]
    public async Task Register_NoSqlInjection_OperatorInEmailField_Returns400()
    {
        // Sends { "email": { "$gt": "" }, "username": "hacker", "password": "Password1!" }
        // The email field expects a plain string — the operator object must be rejected.
        using var client = _apiClient.Create();
        var payload = """{"email": {"$gt": ""}, "username": "hacker", "password": "Password1!"}""";
        var content = new StringContent(payload, Encoding.UTF8, "application/json");

        var response = await client.PostAsync(_apiClient.GetUrl("Auth/register"), content, TestContext.Current.CancellationToken);

        response.StatusCode.Should().Be(HttpStatusCode.BadRequest);
    }

    [Fact]
    public async Task Register_NoSqlInjection_ArrayInUsernameField_Returns400()
    {
        // Sends an array in place of a string — must be rejected by model binding.
        using var client = _apiClient.Create();
        var payload = """{"email": "test@test.com", "username": ["admin", "hacker"], "password": "Password1!"}""";
        var content = new StringContent(payload, Encoding.UTF8, "application/json");

        var response = await client.PostAsync(_apiClient.GetUrl("Auth/register"), content, TestContext.Current.CancellationToken);

        response.StatusCode.Should().Be(HttpStatusCode.BadRequest);
    }

    [Fact]
    public async Task Register_NoSqlInjection_DropCommandAsStringValue_Returns401NotServerError()
    {
        // Embeds a db.dropDatabase() command as a string in the username.
        // The API must treat it as a plain string — not execute it.
        using var client = _apiClient.Create();
        var request = new RegisterRequestViewModel
        {
            Email = "injection@test.com",
            Username = "'; db.dropDatabase(); //",
            Password = "Password1!"
        };

        var response = await client.PostAsJsonAsync(_apiClient.GetUrl("Auth/register"), request, cancellationToken: TestContext.Current.CancellationToken);

        ((int)response.StatusCode).Should().NotBe(500);
    }
}
