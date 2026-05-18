namespace BrewMapEndpointUnitTest.ViewModels.Flag;

public class ReadFlagViewModel
{
    public string Id { get; set; } = String.Empty;
    public string ReportedByUserId { get; set; } = String.Empty;
    public ReadReportTargetViewModel Target { get; set; } = new ReadReportTargetViewModel();
    public string Reason { get; set; }  = String.Empty;
    public string? Description { get; set; }
    public string Status { get; set; } = String.Empty;
    public string? ResolvedByAdminId { get; set; }
    public DateTime? ResolvedAt { get; set; }
    public string? ResolutionNote { get; set; }
    public DateTime CreatedAt { get; set; }
    public DateTime UpdatedAt { get; set; }
}

public class ReadReportTargetViewModel
{
    public string Type { get; set; } = String.Empty;
    public string Id { get; set; } = String.Empty;
}