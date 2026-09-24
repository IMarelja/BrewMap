using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace BrewMapAPI.Models;

public class TempToken
{
    [BsonId]
    [BsonRepresentation(BsonType.ObjectId)]
    public string Id { get; set; } = String.Empty;

    [BsonElement("token")]
    public string Token { get; set; } = String.Empty;

    [BsonElement("userId")]
    [BsonRepresentation(BsonType.ObjectId)]
    public string UserId { get; set; } = String.Empty;

    [BsonElement("expiresAt")]
    public DateTime ExpiresAt { get; set; }
}