using BrewMapAPI.Models;

namespace BrewMapAPI.DTO.Flag
{
    public class ReadFlag
    {
        public string Id { get; set; }
        public string ReportedByUserId { get; set; }
        public ContentType ContentType { get; set; }
        public string ContentId { get; set; }
        public ContentSnapshot ContentSnapshot { get; set; }
        public string Reason { get; set; }
        public FlagStatus Status { get; set; }
        public DateTime CreatedAt { get; set; }
        public DateTime? ReviewedAt { get; set; }
        public string? ReviewedByUserId { get; set; }
    }
}