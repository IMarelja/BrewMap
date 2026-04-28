using BrewMapAPI.Models;

namespace BrewMapAPI.DTO.Flag
{
    public class ReadFlag
    {
        public string Id { get; set; }
        public string ReportedByUserId { get; set; }
        public ReportTarget Target { get; set; }
        public string Reason { get; set; }
        public string? Description { get; set; }
        public string Status { get; set; }
        public string? ResolvedByAdminId { get; set; }
        public DateTime? ResolvedAt { get; set; }
        public string? ResolutionNote { get; set; }
        public DateTime CreatedAt { get; set; }
        public DateTime UpdatedAt { get; set; }
    }
}