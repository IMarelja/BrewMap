using BrewMapAPI.Data;
using BrewMapAPI.Models;
using MongoDB.Driver;

namespace BrewMapAPI.Repository.Users;

public class UserRepo : IUserRepo
{
    private readonly MongoDbContext _context;

    public UserRepo(MongoDbContext dbContext)
    {
        _context = dbContext;
    }

    public async Task<User?> GetUserById(string id)
    {
        return await _context.Users.Find(x => x.Id == id).FirstOrDefaultAsync();
    }

    public async Task<bool> UpdatePassword(string id, string newPasswordHash, string newPasswordSalt)
    {
        var update = Builders<User>.Update
            .Set(x => x.PasswordHash, newPasswordHash)
            .Set(x => x.PasswordSalt, newPasswordSalt);

        var result = await _context.Users.UpdateOneAsync(x => x.Id == id, update);
        return result.ModifiedCount > 0;
    }

    public async Task<bool> UpdateEmail(string id, string newEmail)
    {
        var update = Builders<User>.Update
            .Set(x => x.Email, newEmail);

        var result = await _context.Users.UpdateOneAsync(x => x.Id == id, update);
        return result.ModifiedCount > 0;
    }

    public async Task<bool> UpdateUsername(string id, string newUsername)
    {
        var update = Builders<User>.Update
            .Set(x => x.Username, newUsername);

        var result = await _context.Users.UpdateOneAsync(x => x.Id == id, update);
        return result.ModifiedCount > 0;
    }

    public async Task<bool> DeleteUser(string id)
    {
        var tombstone = $"deleted_{Guid.NewGuid().ToString("N")[..8]}";

        var update = Builders<User>.Update
            .Set(x => x.Username, tombstone)
            .Set(x => x.Email, $"{tombstone}@deleted.invalid")
            .Set(x => x.PasswordHash, string.Empty)
            .Set(x => x.PasswordSalt, string.Empty)
            .Set(x => x.IsDeleted, true)
            .Set(x => x.DeletedAt, DateTime.UtcNow);

        var result = await _context.Users.UpdateOneAsync(x => x.Id == id, update);
        return result.ModifiedCount > 0;
    }
}
