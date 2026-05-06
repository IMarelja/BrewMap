using BrewMapAPI.Models;

namespace BrewMapAPI.DTO.Flag
{
    public class ReadFlag
    {
        public string Id { get; set; } = String.Empty;
        public string ReportedByUserId { get; set; } = String.Empty;
        public ReadReportTarget Target { get; set; } = new ReadReportTarget();
        public string Reason { get; set; }  = String.Empty;
        public string? Description { get; set; }
        public string Status { get; set; } = String.Empty;
        public string? ResolvedByAdminId { get; set; }
        public DateTime? ResolvedAt { get; set; }
        public string? ResolutionNote { get; set; }
        public DateTime CreatedAt { get; set; }
        public DateTime UpdatedAt { get; set; }
    }

    public class ReadReportTarget
    {
        public string Type { get; set; } = String.Empty;
        public string Id { get; set; } = String.Empty;
    }
}