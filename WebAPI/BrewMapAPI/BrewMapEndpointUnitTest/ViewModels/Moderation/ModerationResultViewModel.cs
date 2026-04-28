namespace BrewMapEndpointUnitTest.ViewModels.Moderation;

public class ModerationResultViewModel
{
    public bool Success { get; set; }
    public string Message { get; set; } = string.Empty;
    public DateTime ActionPerformedAt { get; set; }
}
