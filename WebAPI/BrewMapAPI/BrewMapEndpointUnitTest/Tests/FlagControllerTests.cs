using System.Net;
using System.Net.Http.Json;
using BrewMapEndpointUnitTest.Infrastructure;
using BrewMapEndpointUnitTest.ViewModels.Flag;
using FluentAssertions;
using Xunit;

namespace BrewMapEndpointUnitTest.Tests;

public class FlagControllerTests(ITestOutputHelper output)
{
    private readonly ApiClient _apiClient = new();

    [Fact]
    public async Task CreateFlag_AsValidUser_Returns200()
    {
        var token = string.Empty;
        try { token = await _apiClient.GetUserTokenAsync(); }
        catch { Assert.Skip("Could not fetch user token — is the API running and configured?"); }
        using var client = _apiClient.CreateAuthenticated(token);

        var ct = TestContext.Current.CancellationToken;
        var target = new ReportTargetDto() { Id = _apiClient.ValidLocationId, Type = "location"};
        var request = new CreateFlagViewModel() { Target = target, Reason = "Test reason"};
        var response = await client.PostAsJsonAsync(_apiClient.GetUrl("Category"), request, ct);
        var body = await response.Content.ReadFromJsonAsync<ReadFlagViewModel>(ApiClient.GetJsonOptions(), cancellationToken: TestContext.Current.CancellationToken);

        response.StatusCode.Should().Be(HttpStatusCode.Created);
        body.Should().NotBeNull();
        body.Target.Should().Be(target);
        body.Reason.Should().Be("Test reason");
    }

    [Fact]
    public async Task GetById_AsValidAdmin_Returns200WithFlag()
    {
        var token = string.Empty;
        try { token = await _apiClient.GetAdminTokenAsync(); }
        catch { Assert.Skip("Could not fetch admin token — is the API running and configured?"); }
        using var client = _apiClient.CreateAuthenticated(token);
        
        var response = await client.GetAsync(_apiClient.GetUrl($"Flag/{_apiClient.ValidFlagId}"), TestContext.Current.CancellationToken);
        var body = await response.Content.ReadFromJsonAsync<ReadFlagViewModel>(ApiClient.GetJsonOptions(), cancellationToken: TestContext.Current.CancellationToken);
        
        response.StatusCode.Should().Be(HttpStatusCode.OK);
        body.Should().NotBeNull();
        body.Id.Should().Be(_apiClient.ValidFlagId);
        body.ReportedByUserId.Should().NotBeNullOrEmpty();
    }
    
    [Fact]
    public async Task GetById_AsValidAdmin_InvalidId_Returns404()
    {
        var token = string.Empty;
        try { token = await _apiClient.GetAdminTokenAsync(); }
        catch { Assert.Skip("Could not fetch admin token — is the API running and configured?"); }
        using var client = _apiClient.CreateAuthenticated(token);
        
        var response = await client.GetAsync(_apiClient.GetUrl($"Flag/nonexistentid"), TestContext.Current.CancellationToken);
        response.StatusCode.Should().Be(HttpStatusCode.NotFound);
    }
    
    [Fact]
    public async Task GetAll_AsValidAdmin_Returns200()
    {
        var token = string.Empty;
        try { token = await _apiClient.GetAdminTokenAsync(); }
        catch { Assert.Skip("Could not fetch admin token — is the API running and configured?"); }
        using var client = _apiClient.CreateAuthenticated(token);
        
        var response = await client.GetAsync(_apiClient.GetUrl("Flag"), TestContext.Current.CancellationToken);
        response.StatusCode.Should().Be(HttpStatusCode.OK);
    }
    
    [Fact]
    public async Task UpdateFlag_AsValidAdmin_Returns200()
    {
        var token = string.Empty;
        try { token = await _apiClient.GetAdminTokenAsync(); }
        catch { Assert.Skip("Could not fetch admin token — is the API running and configured?"); }
        using var client = _apiClient.CreateAuthenticated(token);
        
        var request = new UpdateFlagViewModel() { Id = _apiClient.ValidFlagId, Status = "reviewed", ResolvedByAdminId = _apiClient.ValidAdminId};
        var response = await client.PutAsJsonAsync(_apiClient.GetUrl($"Flag/{_apiClient.ValidFlagId}"), request,  TestContext.Current.CancellationToken);
        var body = await response.Content.ReadFromJsonAsync<ReadFlagViewModel>(ApiClient.GetJsonOptions(), cancellationToken: TestContext.Current.CancellationToken);

        response.StatusCode.Should().Be(HttpStatusCode.OK);
        body.Should().NotBeNull();
        body.Id.Should().Be(_apiClient.ValidFlagId);
        body.Status.Should().Be("reviewed");
        body.ResolvedByAdminId.Should().Be(_apiClient.ValidAdminId);
    }
    
    [Fact]
    public async Task UpdateFlag_AsValidAdmin_InvalidId_Returns404()
    {
        var token = string.Empty;
        try { token = await _apiClient.GetAdminTokenAsync(); }
        catch { Assert.Skip("Could not fetch admin token — is the API running and configured?"); }
        using var client = _apiClient.CreateAuthenticated(token);
        
        var request = new UpdateFlagViewModel() { Id = _apiClient.ValidFlagId, Status = "reviewed", ResolvedByAdminId = _apiClient.ValidAdminId};
        var response = await client.PutAsJsonAsync(_apiClient.GetUrl($"Flag/nonexistentid"), request,  TestContext.Current.CancellationToken);
        response.StatusCode.Should().Be(HttpStatusCode.NotFound);
    }

    [Fact]
    public async Task GetStats_AsValidAdmin_Returns200()
    {
        var token = string.Empty;
        try { token = await _apiClient.GetAdminTokenAsync(); }
        catch { Assert.Skip("Could not fetch admin token — is the API running and configured?"); }
        using var client = _apiClient.CreateAuthenticated(token);
        
        var response = await client.GetAsync(_apiClient.GetUrl("Flag/stats"), TestContext.Current.CancellationToken);
        response.StatusCode.Should().Be(HttpStatusCode.OK);
        
    }
}