namespace BrewMapEndpointUnitTest.ViewModels.Review;

public class ReadReviewViewModel
{
    public string Id { get; set; } = string.Empty;
    public string UserId { get; set; } = string.Empty;
    public string TargetType { get; set; } = string.Empty;
    public string TargetId { get; set; } = string.Empty;
    public int Rating { get; set; }
    public string? Comment { get; set; }
    public bool IsVisible { get; set; }
}
