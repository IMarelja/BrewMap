using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace BrewMapAPI.Models;

public class TempToken
{
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    public string Id { get; set; }

    [BsonElement("token")]
    public string Token { get; set; }

    [BsonElement("userId")]
    [BsonRepresentation(BsonType.ObjectId)]
    public string UserId { get; set; }

    [BsonElement("expiresAt")]
    public DateTime ExpiresAt { get; set; }
}