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
            // users
            Users.Indexes.CreateMany([
                new CreateIndexModel<User>(
                    Builders<User>.IndexKeys.Ascending(x => x.Email),
                    new CreateIndexOptions { Unique = true }),
                new CreateIndexModel<User>(
                    Builders<User>.IndexKeys.Ascending(x => x.Username),
                    new CreateIndexOptions { Unique = true }),
            ]);

            // products
            Drinks.Indexes.CreateOne(new CreateIndexModel<Drink>(
                Builders<Drink>.IndexKeys
                    .Ascending(x => x.AvailableAtLocationId)
                    .Ascending(x => x.Name),
                new CreateIndexOptions { Unique = true }
            ));

            // locations
            Locations.Indexes.CreateMany([
                new CreateIndexModel<Location>(Builders<Location>.IndexKeys.Ascending(x => x.Name)),
                new CreateIndexModel<Location>(Builders<Location>.IndexKeys.Ascending(x => x.Address.City)),
                new CreateIndexModel<Location>(Builders<Location>.IndexKeys.Ascending(x => x.AggregatedRating.Average)),
                new CreateIndexModel<Location>(Builders<Location>.IndexKeys.Ascending(x => x.PaymentOptionTags)),
                new CreateIndexModel<Location>(Builders<Location>.IndexKeys.Geo2DSphere(x => x.LocationPoint)),
                new CreateIndexModel<Location>(
                    Builders<Location>.IndexKeys
                        .Ascending(x => x.CategoryTag)
                        .Ascending(x => x.IsActive)),
            ]);

            // reviews
            Reviews.Indexes.CreateMany([
                new CreateIndexModel<Review>(
                    Builders<Review>.IndexKeys
                        .Ascending(x => x.Target.TargetId)
                        .Ascending(x => x.Target.Type)),
                new CreateIndexModel<Review>(Builders<Review>.IndexKeys.Ascending(x => x.UserId)),
            ]);

            // categories
            Categories.Indexes.CreateOne(new CreateIndexModel<Category>(
                Builders<Category>.IndexKeys.Ascending(x => x.Tag),
                new CreateIndexOptions { Unique = true }
            ));

            // payment_options
            PaymentOptions.Indexes.CreateOne(new CreateIndexModel<PaymentOption>(
                Builders<PaymentOption>.IndexKeys.Ascending(x => x.Tag),
                new CreateIndexOptions { Unique = true }
            ));

            // reports — two separate non-unique indexes (status alone for admin dashboard;
            // target compound for per-entity report fetching)
            Flags.Indexes.CreateMany([
                new CreateIndexModel<Flag>(Builders<Flag>.IndexKeys.Ascending(x => x.Status)),
                new CreateIndexModel<Flag>(
                    Builders<Flag>.IndexKeys
                        .Ascending(x => x.Target.TargetId)
                        .Ascending(x => x.Target.Type)),
            ]);

            // temp_tokens
            TempTokens.Indexes.CreateMany([
                new CreateIndexModel<TempToken>(
                    Builders<TempToken>.IndexKeys.Ascending(x => x.ExpiresAt),
                    new CreateIndexOptions { ExpireAfter = TimeSpan.Zero }),
                new CreateIndexModel<TempToken>(
                    Builders<TempToken>.IndexKeys.Ascending(x => x.Token),
                    new CreateIndexOptions { Unique = true }),
            ]);
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