namespace BrewMapAPI.DTO.Review
{
    public class UpdateReview
    {
        public string Id { get; set; } = string.Empty;
        public int? Rating { get; set; }
        public string? Comment { get; set; }
        public bool? IsVisible { get; set; }
    }
}