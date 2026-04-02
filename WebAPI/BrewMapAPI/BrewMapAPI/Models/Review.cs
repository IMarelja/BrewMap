using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;
using System;

namespace BrewMapAPI.Models
{
    public class Review
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string Id { get; set; }

        [BsonElement("userId")]
        [BsonRepresentation(BsonType.ObjectId)]
        public string UserId { get; set; }

        [BsonElement("target")]
        public ReviewTarget Target { get; set; }

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
        public string Type { get; set; } // E.g. "location" or "product"

        [BsonElement("id")]
        [BsonRepresentation(BsonType.ObjectId)]
        public string Id { get; set; }
    }
}