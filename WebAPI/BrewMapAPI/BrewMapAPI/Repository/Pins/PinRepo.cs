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
            return await _context.Locations
                .Find(x => x.Id == id)
                .Project(x => new Pin
                {
                    Id = x.Id,
                    Longitude = x.LocationPoint.Coordinates.Longitude,
                    Latitude = x.LocationPoint.Coordinates.Latitude
                })
                .FirstOrDefaultAsync();
        }

        public async Task<List<Pin>> GetByRange(double minLat, double maxLat, double minLon, double maxLon)
        {
            var filter = Builders<Location>.Filter.And(
                Builders<Location>.Filter.Gte(x => x.LocationPoint.Coordinates.Latitude, minLat),
                Builders<Location>.Filter.Lte(x => x.LocationPoint.Coordinates.Latitude, maxLat),
                Builders<Location>.Filter.Gte(x => x.LocationPoint.Coordinates.Longitude, minLon),
                Builders<Location>.Filter.Lte(x => x.LocationPoint.Coordinates.Longitude, maxLon)
            );

            return await _context.Locations
                .Find(filter)
                .Project(x => new Pin
                {
                    Id = x.Id,
                    Longitude = x.LocationPoint.Coordinates.Longitude,
                    Latitude = x.LocationPoint.Coordinates.Latitude
                })
                .ToListAsync();
        }
    }
}