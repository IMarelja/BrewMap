using System.Net;
using System.Net.Http.Json;
using BrewMapEndpointUnitTest.Infrastructure;
using BrewMapEndpointUnitTest.ViewModels.Category;
using FluentAssertions;
using Xunit;

namespace BrewMapEndpointUnitTest.Tests;

public class CategoryControllerTests
{
    private readonly ApiClient _apiClient = new();

    [Fact]
    public async Task GetAll_AsValidUser_Returns200()
    {
        var token = await _apiClient.GetUserTokenAsync();
        using var client = _apiClient.CreateAuthenticated(token);

        var response = await client.GetAsync(_apiClient.GetUrl("Category"));

        response.StatusCode.Should().Be(HttpStatusCode.OK);
    }

    [Fact]
    public async Task GetByTag_AsValidUser_Returns200()
    {
        var token = await _apiClient.GetUserTokenAsync();
        using var client = _apiClient.CreateAuthenticated(token);

        var response = await client.GetAsync(_apiClient.GetUrl($"Category/{_apiClient.ValidCategoryTag}"));
        var body = await response.Content.ReadFromJsonAsync<ReadCategoryViewModel>(ApiClient.GetJsonOptions());

        response.StatusCode.Should().Be(HttpStatusCode.OK);
        body.Should().NotBeNull();
        body!.Tag.Should().Be(_apiClient.ValidCategoryTag);
    }

    [Fact]
    public async Task GetByTag_AsValidUser_InvalidTag_Returns404()
    {
        var token = await _apiClient.GetUserTokenAsync();
        using var client = _apiClient.CreateAuthenticated(token);

        var response = await client.GetAsync(_apiClient.GetUrl("Category/nonexistenttag"));

        response.StatusCode.Should().Be(HttpStatusCode.NotFound);
    }

    [Fact]
    public async Task Create_AsValidAdmin_ValidEntry_Returns201()
    {
        var token = await _apiClient.GetAdminTokenAsync();
        using var client = _apiClient.CreateAuthenticated(token);

        var request = new CreateCategoryViewModel
        {
            Tag = "cocktails",
            Name = "Cocktails"
        };
        var response = await client.PostAsJsonAsync(_apiClient.GetUrl("Category"), request);
        var body = await response.Content.ReadFromJsonAsync<ReadCategoryViewModel>(ApiClient.GetJsonOptions());

        response.StatusCode.Should().Be(HttpStatusCode.Created);
        body.Should().NotBeNull();
        body!.Tag.Should().Be("cocktails");
        body.Name.Should().Be("Cocktails");
    }

    [Fact]
    public async Task Create_AsValidAdmin_DuplicateTag_Returns400()
    {
        var token = await _apiClient.GetAdminTokenAsync();
        using var client = _apiClient.CreateAuthenticated(token);

        var request = new CreateCategoryViewModel
        {
            Tag = _apiClient.ValidCategoryTag,
            Name = "Coffee"
        };
        var response = await client.PostAsJsonAsync(_apiClient.GetUrl("Category"), request);

        response.StatusCode.Should().Be(HttpStatusCode.BadRequest);
    }

    [Fact]
    public async Task Create_AsValidUser_Returns403()
    {
        var token = await _apiClient.GetUserTokenAsync();
        using var client = _apiClient.CreateAuthenticated(token);

        var request = new CreateCategoryViewModel
        {
            Tag = "smoothies",
            Name = "Smoothies"
        };
        var response = await client.PostAsJsonAsync(_apiClient.GetUrl("Category"), request);

        response.StatusCode.Should().Be(HttpStatusCode.Forbidden);
    }

    [Fact]
    public async Task Update_AsValidAdmin_ValidTag_Returns200()
    {
        var token = await _apiClient.GetAdminTokenAsync();
        using var client = _apiClient.CreateAuthenticated(token);

        var request = new EditCategoryViewModel
        {
            Name = "Coffee & Espresso"
        };
        var response = await client.PutAsJsonAsync(_apiClient.GetUrl($"Category/{_apiClient.ValidCategoryTag}"), request);
        var body = await response.Content.ReadFromJsonAsync<ReadCategoryViewModel>(ApiClient.GetJsonOptions());

        response.StatusCode.Should().Be(HttpStatusCode.OK);
        body.Should().NotBeNull();
        body!.Name.Should().Be("Coffee & Espresso");
    }

    [Fact]
    public async Task Update_AsValidAdmin_InvalidTag_Returns404()
    {
        var token = await _apiClient.GetAdminTokenAsync();
        using var client = _apiClient.CreateAuthenticated(token);

        var request = new EditCategoryViewModel
        {
            Name = "Does Not Exist"
        };
        var response = await client.PutAsJsonAsync(_apiClient.GetUrl("Category/nonexistenttag"), request);

        response.StatusCode.Should().Be(HttpStatusCode.NotFound);
    }
}
