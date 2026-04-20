using BrewMapAPI.Data;
using BrewMapAPI.Models;
using MongoDB.Driver;

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

        public async Task DeleteAsync(string id)
        {
            var update = Builders<Location>.Update.Set(l => l.IsActive, false);
            await _locations.UpdateOneAsync(l => l.Id == id, update);
        }
    }
}
