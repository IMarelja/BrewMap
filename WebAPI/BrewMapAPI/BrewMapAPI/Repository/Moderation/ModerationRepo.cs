using BrewMapAPI.Data;
using BrewMapAPI.Models;
using MongoDB.Driver;

namespace BrewMapAPI.Repository.Moderation
{
    public class ModerationRepo : IModerationRepo
    {
        private readonly MongoDbContext _context;

        public ModerationRepo(MongoDbContext context)
        {
            _context = context;
        }

        //Content management

        public async Task<bool> DeleteLocation(string id)
        {
            var result = await _context.Locations.DeleteOneAsync(x => x.Id == id);
            return result.DeletedCount > 0;
        }

        public async Task<bool> DeleteDrink(string id)
        {
            var result = await _context.Drinks.DeleteOneAsync(x => x.Id == id);
            return result.DeletedCount > 0;
        }

        public async Task<bool> DeleteReview(string id)
        {
            var result = await _context.Reviews.DeleteOneAsync(x => x.Id == id);
            return result.DeletedCount > 0;
        }

        //User management

        public async Task<User?> GetUserById(string id)
        {
            return await _context.Users.Find(x => x.Id == id).FirstOrDefaultAsync();
        }

        public async Task<User?> UpdateUserRole(string userId, string newRole)
        {
            var update = Builders<User>.Update.Set(x => x.Role, newRole);
            var options = new FindOneAndUpdateOptions<User> { ReturnDocument = ReturnDocument.After };
            
            return await _context.Users.FindOneAndUpdateAsync(
                x => x.Id == userId, 
                update, 
                options
            );
        }
    }
}