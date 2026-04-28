using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;
using System;

namespace BrewMapAPI.Models
{
    public class Drink
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string Id { get; set; } = String.Empty;

        [BsonElement("name")]
        public string Name { get; set; } = String.Empty;

        [BsonElement("description")]
        public string? Description { get; set; }

        [BsonElement("availableAtLocationId")]
        [BsonRepresentation(BsonType.ObjectId)]
        public string AvailableAtLocationId { get; set; } = String.Empty;

        [BsonElement("isVisible")]
        public bool IsVisible { get; set; } = true;

        [BsonElement("reportCount")]
        public int ReportCount { get; set; } = 0;

        [BsonElement("createdByUserId")]
        [BsonRepresentation(BsonType.ObjectId)]
        public string CreatedByUserId { get; set; } = String.Empty;

        [BsonElement("createdAt")]
        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

        [BsonElement("updatedAt")]
        public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;

        [BsonElement("aggregatedRating")]
        public AggregatedRating AggregatedRating { get; set; } = new AggregatedRating();
    }

    public class AggregatedRating
    {
        [BsonElement("average")]
        public double Average { get; set; } = 0;

        [BsonElement("count")]
        public int Count { get; set; } = 0;
    }
}