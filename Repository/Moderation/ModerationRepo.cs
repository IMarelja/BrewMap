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

        public async Task<User?> UpdateUserIsActive(string userId, bool isActive)
        {
            var update = Builders<User>.Update.Set(x => x.IsActive, isActive);
            var options = new FindOneAndUpdateOptions<User> { ReturnDocument = ReturnDocument.After };

            return await _context.Users.FindOneAndUpdateAsync(
                x => x.Id == userId,
                update,
                options
            );
        }
    }
}