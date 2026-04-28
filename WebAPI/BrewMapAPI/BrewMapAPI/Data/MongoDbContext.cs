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
            // Drink (products)
            var drinksIndex = Builders<Drink>.IndexKeys
                .Ascending(x => x.AvailableAtLocationId)
                .Ascending(x => x.Name);

            Drinks.Indexes.CreateOne(new CreateIndexModel<Drink>(
                drinksIndex,
                new CreateIndexOptions { Unique = true }
            ));
            //index for locations
            var nameIndex = Builders<Location>.IndexKeys.Ascending(x => x.Name);
            var cityIndex = Builders<Location>.IndexKeys.Ascending(x => x.Address.City);
            var ratingIndex = Builders<Location>.IndexKeys.Ascending(x => x.AggregatedRating.Average);
            var paymentIndex = Builders<Location>.IndexKeys.Ascending(x => x.PaymentOptionTags);
            var geoIndex = Builders<Location>.IndexKeys.Geo2DSphere(x => x.LocationPoint);

            Locations.Indexes.CreateMany(new[]
            {
                new CreateIndexModel<Location>(nameIndex),
                new CreateIndexModel<Location>(cityIndex),
                new CreateIndexModel<Location>(ratingIndex),
                new CreateIndexModel<Location>(paymentIndex),
                new CreateIndexModel<Location>(geoIndex)
            });

            var flagsIndex = Builders<Flag>.IndexKeys
                .Ascending(x => x.Status)
                .Ascending(x => x.Target.Id)
                .Ascending(x => x.Target.Type);

            Flags.Indexes.CreateOne(new CreateIndexModel<Flag>(
                flagsIndex,
                new CreateIndexOptions { Unique = true }
            ));
        }

        public IMongoCollection<User> Users => _database.GetCollection<User>("users");
        public IMongoCollection<Drink> Drinks => _database.GetCollection<Drink>("products");
        public IMongoCollection<Location> Locations => _database.GetCollection<Location>("locations");
        public IMongoCollection<Review> Reviews => _database.GetCollection<Review>("reviews");
        public IMongoCollection<TempToken> TempTokens => _database.GetCollection<TempToken>("temp_tokens");
        public IMongoCollection<Category> Categories => _database.GetCollection<Category>("categories");
        public IMongoCollection<PaymentOption> PaymentOptions => _database.GetCollection<PaymentOption>("payment_options");
        public IMongoCollection<Flag> Flags => _database.GetCollection<Flag>("reports");

    }
}