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
        var token = await _apiClient.GetUserTokenAsync();
        using var client = _apiClient.CreateAuthenticated(token);

        var response = await client.GetAsync(_apiClient.GetUrl("PaymentOption"));

        response.StatusCode.Should().Be(HttpStatusCode.OK);
    }

    [Fact]
    public async Task GetByTag_AsValidUser_Returns200()
    {
        var token = await _apiClient.GetUserTokenAsync();
        using var client = _apiClient.CreateAuthenticated(token);

        var response = await client.GetAsync(_apiClient.GetUrl($"PaymentOption/{_apiClient.ValidPaymentOptionTag}"));
        var body = await response.Content.ReadFromJsonAsync<ReadPaymentOptionViewModel>(ApiClient.GetJsonOptions());

        response.StatusCode.Should().Be(HttpStatusCode.OK);
        body.Should().NotBeNull();
        body!.Tag.Should().Be(_apiClient.ValidPaymentOptionTag);
    }

    [Fact]
    public async Task Create_AsValidUser_Returns403()
    {
        var token = await _apiClient.GetUserTokenAsync();
        using var client = _apiClient.CreateAuthenticated(token);

        var request = new CreatePaymentOptionViewModel { 
            Tag = "crypto", 
            Name = "Crypto" 
        };
        var response = await client.PostAsJsonAsync(_apiClient.GetUrl("PaymentOption"), request);

        response.StatusCode.Should().Be(HttpStatusCode.Forbidden);
    }

    [Fact]
    public async Task Create_AsValidAdmin_ValidEntry_Returns201()
    {
        var token = await _apiClient.GetAdminTokenAsync();
        using var client = _apiClient.CreateAuthenticated(token);

        var request = new CreatePaymentOptionViewModel { 
            Tag = "crypto", 
            Name = "Crypto" 
        };
        var response = await client.PostAsJsonAsync(_apiClient.GetUrl("PaymentOption"), request);
        var body = await response.Content.ReadFromJsonAsync<ReadPaymentOptionViewModel>(ApiClient.GetJsonOptions());

        response.StatusCode.Should().Be(HttpStatusCode.Created);
        body.Should().NotBeNull();
        body.Tag.Should().Be("crypto");
        body.Name.Should().Be("Crypto");
    }

    [Fact]
    public async Task Create_AsValidAdmin_DuplicateTag_Returns409()
    {
        var token = await _apiClient.GetAdminTokenAsync();
        using var client = _apiClient.CreateAuthenticated(token);

        var request = new CreatePaymentOptionViewModel { 
            Tag = "cash", 
            Name = "Cash payment" 
        };
        var response = await client.PostAsJsonAsync(_apiClient.GetUrl("PaymentOption"), request);

        response.StatusCode.Should().Be(HttpStatusCode.BadRequest);
    }

    [Fact]
    public async Task Update_AsValidAdmin_ValidTag_Returns200()
    {
        var token = await _apiClient.GetAdminTokenAsync();
        using var client = _apiClient.CreateAuthenticated(token);

        var tag = "cash";
        var request = new EditPaymentOptionViewModel { 
            Name = "Cash payment" 
        };
        var response = await client.PutAsJsonAsync(_apiClient.GetUrl($"PaymentOption/{tag}"), request);
        var body = await response.Content.ReadFromJsonAsync<ReadPaymentOptionViewModel>(ApiClient.GetJsonOptions());

        response.StatusCode.Should().Be(HttpStatusCode.OK);
        body.Should().NotBeNull();
        body.Name.Should().Be("Cash payment");
    }

    [Fact]
    public async Task Update_AsValidAdmin_InvalidTag_Returns404()
    {
        var token = await _apiClient.GetAdminTokenAsync();
        using var client = _apiClient.CreateAuthenticated(token);

        var tag = "blood";
        var request = new EditPaymentOptionViewModel { 
            Name = "Blood" 
        };
        var response = await client.PutAsJsonAsync(_apiClient.GetUrl($"PaymentOption/{tag}"), request);

        response.StatusCode.Should().Be(HttpStatusCode.NotFound);
    }
}
