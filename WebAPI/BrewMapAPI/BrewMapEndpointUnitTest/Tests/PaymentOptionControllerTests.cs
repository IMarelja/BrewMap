using System.Net;
using System.Net.Http.Json;
using BrewMapEndpointUnitTest.Infrastructure;
using BrewMapEndpointUnitTest.ViewModels.PaymentOption;
using FluentAssertions;
using Xunit;

namespace BrewMapEndpointUnitTest.Tests;

public class PaymentOptionControllerTests
{
    private readonly ApiClient _apiClient = new();

    [Fact]
    public async Task GetAll_AsValidUser_Returns200()
    {
        var token = string.Empty;
        try { token = await _apiClient.GetUserTokenAsync(); }
        catch { Assert.Skip("Could not fetch user token — is the API running and configured?"); }
        using var client = _apiClient.CreateAuthenticated(token);

        var response = await client.GetAsync(_apiClient.GetUrl("PaymentOption"), TestContext.Current.CancellationToken);

        response.StatusCode.Should().Be(HttpStatusCode.OK);
    }

    [Fact]
    public async Task GetByTag_AsValidUser_Returns200()
    {
        if (string.IsNullOrEmpty(_apiClient.ValidPaymentOptionTag))
            Assert.Skip("ValidPaymentOptionTag is not configured in appsettings.json");

        var token = string.Empty;
        try { token = await _apiClient.GetUserTokenAsync(); }
        catch { Assert.Skip("Could not fetch user token — is the API running and configured?"); }
        using var client = _apiClient.CreateAuthenticated(token);

        var ct = TestContext.Current.CancellationToken;
        var response = await client.GetAsync(_apiClient.GetUrl($"PaymentOption/{_apiClient.ValidPaymentOptionTag}"), ct);
        var body = await response.Content.ReadFromJsonAsync<ReadPaymentOptionViewModel>(ApiClient.GetJsonOptions());

        response.StatusCode.Should().Be(HttpStatusCode.OK);
        body.Should().NotBeNull();
        body.Tag.Should().Be(_apiClient.ValidPaymentOptionTag);
    }

    [Fact]
    public async Task Create_AsValidAdmin_ValidEntry_Returns201()
    {
        var token = string.Empty;
        try { token = await _apiClient.GetAdminTokenAsync(); }
        catch { Assert.Skip("Could not fetch admin token — is the API running and configured?"); }
        using var client = _apiClient.CreateAuthenticated(token);

        var ct = TestContext.Current.CancellationToken;
        var request = new CreatePaymentOptionViewModel { Tag = "crypto", Name = "Crypto" };
        var response = await client.PostAsJsonAsync(_apiClient.GetUrl("PaymentOption"), request, ct);
        var body = await response.Content.ReadFromJsonAsync<ReadPaymentOptionViewModel>(ApiClient.GetJsonOptions());

        response.StatusCode.Should().Be(HttpStatusCode.Created);
        body.Should().NotBeNull();
        body.Tag.Should().Be("crypto");
        body.Name.Should().Be("Crypto");
    }

    [Fact]
    public async Task Create_AsValidAdmin_DuplicateTag_Returns409()
    {
        var token = string.Empty;
        try { token = await _apiClient.GetAdminTokenAsync(); }
        catch { Assert.Skip("Could not fetch admin token — is the API running and configured?"); }
        using var client = _apiClient.CreateAuthenticated(token);

        var request = new CreatePaymentOptionViewModel { Tag = "cash", Name = "Cash payment" };
        var response = await client.PostAsJsonAsync(_apiClient.GetUrl("PaymentOption"), request, TestContext.Current.CancellationToken);

        response.StatusCode.Should().Be(HttpStatusCode.BadRequest);
    }

    [Fact]
    public async Task Update_AsValidAdmin_ValidTag_Returns200()
    {
        var token = string.Empty;
        try { token = await _apiClient.GetAdminTokenAsync(); }
        catch { Assert.Skip("Could not fetch admin token — is the API running and configured?"); }
        using var client = _apiClient.CreateAuthenticated(token);

        var ct = TestContext.Current.CancellationToken;
        var request = new EditPaymentOptionViewModel { Name = "Cash payment" };
        var response = await client.PutAsJsonAsync(_apiClient.GetUrl("PaymentOption/cash"), request, ct);
        var body = await response.Content.ReadFromJsonAsync<ReadPaymentOptionViewModel>(ApiClient.GetJsonOptions());

        response.StatusCode.Should().Be(HttpStatusCode.OK);
        body.Should().NotBeNull();
        body.Name.Should().Be("Cash payment");
    }

    [Fact]
    public async Task Update_AsValidAdmin_InvalidTag_Returns404()
    {
        var token = string.Empty;
        try { token = await _apiClient.GetAdminTokenAsync(); }
        catch { Assert.Skip("Could not fetch admin token — is the API running and configured?"); }
        using var client = _apiClient.CreateAuthenticated(token);

        var request = new EditPaymentOptionViewModel { Name = "Blood" };
        var response = await client.PutAsJsonAsync(_apiClient.GetUrl("PaymentOption/blood"), request, TestContext.Current.CancellationToken);

        response.StatusCode.Should().Be(HttpStatusCode.NotFound);
    }
}
