namespace BrewMapEndpointUnitTest.ViewModels.Drinks;

public class ReadDrinkViewModel
{
    public string Id { get; set; } = string.Empty;
    public string Name { get; set; } = string.Empty;
    public string? Description { get; set; }
    public string AvailableAtLocationId { get; set; } = string.Empty;
    public string CreatedByUserId { get; set; } = string.Empty;
    public DateTime CreatedAt { get; set; }
    public DateTime UpdatedAt { get; set; }
    public AggregatedRatingViewModel AggregatedRating { get; set; } = new();
}

public class AggregatedRatingViewModel
{
    public double Average { get; set; }
    public int Count { get; set; }
}
