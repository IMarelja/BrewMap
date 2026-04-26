using BrewMapAPI.Data;
using BrewMapAPI.Models;
using MongoDB.Bson;
using MongoDB.Driver;
using MongoDB.Driver.GeoJsonObjectModel;

namespace BrewMapAPI.Repository.Locations
{
    public class LocationRepo : ILocationRepo
    {
        private readonly MongoDbContext _context;
        private readonly IMongoCollection<Location> _locations;

        public LocationRepo(MongoDbContext context)
        {
            _context = context;
            _locations = _context.Locations;
        }

        public async Task<Location> CreateAsync(Location location)
        {
            await _locations.InsertOneAsync(location);
            return location;
        }

        public async Task<IEnumerable<Location>> GetAllAsync()
        {
            return await _locations.Find(l => l.IsActive).ToListAsync();
        }

        public async Task<IEnumerable<Location>> SearchAsync(
            string? query, double? minRating, List<string>? paymentOptionTags,
            double? centerLatitude, double? centerLongitude,
            double radiusMeters)
        {
            var filter = Builders<Location>.Filter.Eq(l => l.IsActive, true);

            if (!string.IsNullOrWhiteSpace(query))
            {
                var regex = new BsonRegularExpression(query.Trim(), "i");
                filter &= Builders<Location>.Filter.Or(
                    Builders<Location>.Filter.Regex(l => l.Name, regex),
                    Builders<Location>.Filter.Regex(l => l.Description, regex)
                );
            }

            if (minRating.HasValue)
                filter &= Builders<Location>.Filter.Gte(l => l.AggregatedRating.Average, minRating.Value);

            if (paymentOptionTags != null && paymentOptionTags.Count > 0)
                filter &= Builders<Location>.Filter.All(l => l.PaymentOptionTags, paymentOptionTags);

            if (centerLatitude.HasValue && centerLongitude.HasValue)
                filter &= Builders<Location>.Filter.GeoWithinCenterSphere(
                    l => l.LocationPoint,
                    centerLongitude.Value,
                    centerLatitude.Value,
                    radiusMeters / 6371000.0);

            return await _locations.Find(filter).ToListAsync();
        }

        public async Task<Location?> GetByIdAsync(string id)
        {
            return await _locations
                .Find(l => l.Id == id && l.IsActive)
                .FirstOrDefaultAsync();
        }

        public async Task UpdateAsync(Location location)
        {
            await _locations.ReplaceOneAsync(l => l.Id == location.Id, location);
        }

        public async Task<bool> DeleteAsync(string id)
        {
            var update = Builders<Location>.Update.Set(l => l.IsActive, false);
            var result = await _locations.UpdateOneAsync(l => l.Id == id, update);
            return result.ModifiedCount > 0;
        }
    }
}
