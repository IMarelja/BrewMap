using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace BrewMapAPI.Models
{
    public class Pin
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string Id { get; set; }

        [BsonElement("longitude")]
        public double Longitude { get; set; }

        [BsonElement("latitude")]
        public double Latitude { get; set; }
    }
}