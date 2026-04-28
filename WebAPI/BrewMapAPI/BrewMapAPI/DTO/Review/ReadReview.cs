namespace BrewMapAPI.DTO.Review
{
    public class ReadReview
    {
        public string Id { get; set; } = string.Empty;
        public string UserId { get; set; } = string.Empty;
        public string TargetType { get; set; } = string.Empty; // "location" or "product"
        public string TargetId { get; set; } = string.Empty;   // ID of the cafe or beverage
        public int Rating { get; set; }
        public string? Comment { get; set; }
        public bool IsVisible { get; set; }
        public int ReportCount { get; set; }
        public DateTime CreatedAt { get; set; }
        public DateTime UpdatedAt { get; set; }
    }
}