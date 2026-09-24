using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;
using System;


namespace BrewMapAPI.Models
{
    public class User
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string Id { get; set; } = String.Empty;

        [BsonElement("username")] 
        public string Username { get; set; } = String.Empty;

        [BsonElement("email")] 
        public string Email { get; set; } = String.Empty;

        [BsonElement("passwordHash")] 
        public string PasswordHash { get; set; } = String.Empty;

        [BsonElement("passwordSalt")] 
        public string PasswordSalt { get; set; } = String.Empty;

        [BsonElement("role")] 
        public string Role { get; set; } = "user";

        [BsonElement("isActive")] 
        public bool IsActive { get; set; } = true;

        [BsonElement("createdAt")] 
        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

        [BsonElement("lastLoginAt")]
        [BsonIgnoreIfNull]
        public DateTime? LastLoginAt { get; set; }

        [BsonElement("isDeleted")] 
        public bool IsDeleted { get; set; } = false;

        [BsonElement("deletedAt")]
        [BsonIgnoreIfNull]
        public DateTime? DeletedAt { get; set; }

        [BsonElement("reportCount")] 
        public int ReportCount { get; set; } = 0;
    }
}