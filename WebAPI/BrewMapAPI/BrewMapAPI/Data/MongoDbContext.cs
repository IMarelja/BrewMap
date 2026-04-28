using BrewMapAPI.Models;
using MongoDB.Driver;
using Microsoft.Extensions.Configuration;

namespace BrewMapAPI.Data
{
    public class MongoDbContext
    {
        private readonly IMongoDatabase _database;

        public MongoDbContext(IConfiguration configuration)
        {
            var connectionString = configuration.GetSection("DatabaseSettings:ConnectionString").Value;
            var dbName = configuration.GetSection("DatabaseSettings:DatabaseName").Value;
            var client = new MongoClient(connectionString);
            _database = client.GetDatabase(dbName);

            ApplyIndex();
        }

        private void ApplyIndex()
        {
            var drinksIndex = Builders<Drink>.IndexKeys
                .Ascending(x => x.AvailableAtLocationId)
                .Ascending(x => x.Name);

            Drinks.Indexes.CreateOne(new CreateIndexModel<Drink>(
                drinksIndex,
                new CreateIndexOptions { Unique = true }
            ));
        }

        public IMongoCollection<User> Users => _database.GetCollection<User>("users");
        public IMongoCollection<Drink> Drinks => _database.GetCollection<Drink>("products");
        public IMongoCollection<Location> Locations => _database.GetCollection<Location>("locations");
        public IMongoCollection<Review> Reviews => _database.GetCollection<Review>("reviews");
        public IMongoCollection<TempToken> TempTokens => _database.GetCollection<TempToken>("temp_tokens");
    }
}