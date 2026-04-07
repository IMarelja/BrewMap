namespace BrewMapAPI.DTO.Drink
{
    public class ReadDrink
    {
        public string Id { get; set; } = string.Empty;
        public string Name { get; set; } = string.Empty;
        public string? Description { get; set; }
        public string AvailableAtLocationId { get; set; } = string.Empty;
        public string CreatedByUserId { get; set; } = string.Empty;
        public DateTime CreatedAt { get; set; }
        public DateTime UpdatedAt { get; set; }
        public ReadAggregatedRating AggregatedRating { get; set; }
    }

    public class ReadAggregatedRating
    {
        public double Average { get; set; }
        public int Count { get; set; }
    }
}
