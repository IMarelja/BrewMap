namespace BrewMapEndpointUnitTest.ViewModels.Flag;

public class CreateFlagViewModel
{
    public ReportTargetDto Target { get; set; } = new ReportTargetDto();
    public string Reason { get; set; } = string.Empty;
    public string? Description { get; set; }
    
}
public class ReportTargetDto
{
    public string Type { get; set; } = string.Empty;
    public string Id { get; set; } = string.Empty;
}