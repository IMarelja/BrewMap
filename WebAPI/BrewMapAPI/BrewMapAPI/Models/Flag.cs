using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;
using System.ComponentModel.DataAnnotations;

namespace BrewMapAPI.Models
{
    public class Flag
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string Id { get; set; } = String.Empty;

        [BsonElement("reportedByUserId")]
        [BsonRepresentation(BsonType.ObjectId)]
        public string ReportedByUserId { get; set; } = String.Empty;

        [BsonElement("target")]
        public ReportTarget Target { get; set; } = new ReportTarget();

        [BsonElement("reason")]
        public string Reason { get; set; } = String.Empty;

        [BsonElement("description")]
        public string? Description { get; set; }

        [BsonElement("status")]
        public string Status { get; set; } = "pending";

        [BsonElement("resolvedByAdminId")]
        [BsonRepresentation(BsonType.ObjectId)]
        public string? ResolvedByAdminId { get; set; }

        [BsonElement("resolvedAt")]
        public DateTime? ResolvedAt { get; set; }

        [BsonElement("resolutionNote")]
        public string? ResolutionNote { get; set; }

        [BsonElement("createdAt")]
        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

        [BsonElement("updatedAt")]
        public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;
    }

    public class ReportTarget
    {
        [BsonElement("type")]
        [AllowedValues("location", "product", "review", "user")]
        public string Type { get; set; } = String.Empty;

        [BsonElement("id")]
        [BsonRepresentation(BsonType.ObjectId)]
        public string TargetId { get; set; } = String.Empty;
    }
}