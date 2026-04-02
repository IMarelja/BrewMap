using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;
using System;

namespace BrewMapAPI.Models
{
    public class User
    {

        public int? Id { get; set; }
        public string Username { get; set; }
        public string Email { get; set; }
        public string PasswordHash { get; set; }
        public string PasswordSalt { get; set; }
        public string Role { get; set; }
        public DateTime CreatedAt { get; set; }

    }
}