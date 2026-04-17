using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;
using System;

namespace BrewMapAPI.Models
{
    public class Flag
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string Id { get; set; }

        [BsonElement("reportedByUserId")]
        [BsonRepresentation(BsonType.ObjectId)]
        public string ReportedByUserId { get; set; }

        [BsonElement("contentType")]
        public ContentType ContentType { get; set; }

        [BsonElement("contentId")]
        [BsonRepresentation(BsonType.ObjectId)]
        public string ContentId { get; set; }

        [BsonElement("contentSnapshot")]
        public ContentSnapshot ContentSnapshot { get; set; }

        [BsonElement("reason")]
        public string Reason { get; set; }

        [BsonElement("status")]
        public FlagStatus Status { get; set; } = FlagStatus.Pending;

        [BsonElement("createdAt")]
        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

        [BsonElement("reviewedAt")]
        public DateTime? ReviewedAt { get; set; }

        [BsonElement("reviewedByUserId")]
        [BsonRepresentation(BsonType.ObjectId)]
        public string? ReviewedByUserId { get; set; }
    }

    public enum ContentType
    {
        Location,
        Drink,
        Review
    }

    public enum FlagStatus
    {
        Pending,
        Reviewed,
        Resolved
    }

    public class ContentSnapshot
    {
        [BsonElement("name")]
        public string? Name { get; set; }

        [BsonElement("description")]
        public string? Description { get; set; }

        [BsonElement("address")]
        public string? Address { get; set; }

        [BsonElement("rating")]
        public int? Rating { get; set; }

        [BsonElement("comment")]
        public string? Comment { get; set; }

        [BsonElement("createdByUserId")]
        [BsonRepresentation(BsonType.ObjectId)]
        public string? CreatedByUserId { get; set; }
    }
}