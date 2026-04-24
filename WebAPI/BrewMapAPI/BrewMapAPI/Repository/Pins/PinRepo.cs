using BrewMapAPI.Data;
using BrewMapAPI.Models;
using MongoDB.Driver;
using MongoDB.Driver.GeoJsonObjectModel;

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
            var bbox = new GeoJsonPolygon<GeoJson2DGeographicCoordinates>(
                new GeoJsonPolygonCoordinates<GeoJson2DGeographicCoordinates>(
                    new GeoJsonLinearRingCoordinates<GeoJson2DGeographicCoordinates>(
                    [
                        new(minLon, minLat),
                        new(maxLon, minLat),
                        new(maxLon, maxLat),
                        new(minLon, maxLat),
                        new(minLon, minLat)
                    ])
                )
            );

            var filter = Builders<Location>.Filter.GeoWithin(x => x.LocationPoint, bbox);

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