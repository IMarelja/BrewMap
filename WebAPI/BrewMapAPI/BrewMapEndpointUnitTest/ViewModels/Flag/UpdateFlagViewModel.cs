namespace BrewMapEndpointUnitTest.ViewModels.Flag;

public class UpdateFlagViewModel
{
    public string Id { get; set; } = String.Empty;
    public string Status { get; set; } = String.Empty;
    public string ResolvedByAdminId { get; set; } = String.Empty;
    public string? ResolutionNote { get; set; }
}