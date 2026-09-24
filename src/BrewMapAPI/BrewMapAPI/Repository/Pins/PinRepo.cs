using BrewMapAPI.Data;
using BrewMapAPI.Models;
using MongoDB.Driver;

namespace BrewMapAPI.Repository.Pins
{
    public class PinRepo : IPinRepo
    {
        private readonly MongoDbContext _context;

        public PinRepo(MongoDbContext context)
        {
            _context = context;
        }

        public async Task<Pin?> GetById(string id)
        {
            var location = await _context.Locations
                .Find(x => x.Id == id)
                .FirstOrDefaultAsync();

            if (location == null) return null;

            return new Pin
            {
                Id = location.Id,
                Longitude = location.LocationPoint.Coordinates.Longitude,
                Latitude = location.LocationPoint.Coordinates.Latitude
            };
        }

        public async Task<List<Pin>> GetByRange(double minLat, double maxLat, double minLon, double maxLon)
        {
            // GeoWithin with a GeoJSON polygon fails for ranges larger than a hemisphere
            // (MongoDB returns the complement, yielding no results).
            // Simple coordinate array index filters work correctly for any range.
            var filter = Builders<Location>.Filter.And(
                Builders<Location>.Filter.Gte("LocationPoint.coordinates.0", minLon),
                Builders<Location>.Filter.Lte("LocationPoint.coordinates.0", maxLon),
                Builders<Location>.Filter.Gte("LocationPoint.coordinates.1", minLat),
                Builders<Location>.Filter.Lte("LocationPoint.coordinates.1", maxLat)
            );

            var locations = await _context.Locations.Find(filter).ToListAsync();

            return [.. locations.Select(x => new Pin
            {
                Id = x.Id,
                Longitude = x.LocationPoint.Coordinates.Longitude,
                Latitude = x.LocationPoint.Coordinates.Latitude
            })];
        }
    }
}