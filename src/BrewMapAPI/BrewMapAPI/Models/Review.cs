using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;
using System.ComponentModel.DataAnnotations;

namespace BrewMapAPI.Models
{
    public class Review
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string Id { get; set; } = String.Empty;

        [BsonElement("userId")]
        [BsonRepresentation(BsonType.ObjectId)]
        public string UserId { get; set; } = String.Empty;

        [BsonElement("target")]
        public ReviewTarget Target { get; set; } = new ReviewTarget();

        [BsonElement("rating")]
        public int Rating { get; set; } // 1-5

        [BsonElement("comment")]
        public string? Comment { get; set; }

        [BsonElement("isVisible")]
        public bool IsVisible { get; set; } = true;

        [BsonElement("reportCount")]
        public int ReportCount { get; set; } = 0;

        [BsonElement("createdAt")]
        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

        [BsonElement("updatedAt")]
        public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;
    }

  
    public class ReviewTarget
    {
        [BsonElement("type")]
        [AllowedValues("location", "product")]
        public string Type { get; set; } = String.Empty;

        [BsonElement("id")]
        [BsonRepresentation(BsonType.ObjectId)]
        public string TargetId { get; set; } = String.Empty;
    }
}