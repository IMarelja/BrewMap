namespace BrewMapAPI.DTO.Review
{
    public class CreateReview
    {
        public string TargetType { get; set; } = string.Empty; // "location" or "product"
        public string TargetId { get; set; } = string.Empty;
        public int Rating { get; set; }
        public string? Comment { get; set; }
    }
}