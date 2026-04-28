using BrewMapAPI.Data;
using BrewMapAPI.DTO.Auth;
using BrewMapAPI.Models;
using BrewMapAPI.Service.Auth;
using MongoDB.Driver;

namespace BrewMapAPI.Repository.Auth;

public class AuthRepo : IAuthRepo
{
    private MongoDbContext _context;
    
    public AuthRepo(MongoDbContext context)
    {
        _context = context;
    }
    public async Task<User?> GetByUsername(string username)
    {
        return await _context.Users.Find(x => x.Username == username).FirstOrDefaultAsync();
    }

    public async Task<User?> GetByEmail(string email)
    {
        return await _context.Users.Find(x => x.Email == email).FirstOrDefaultAsync();
    }

    public async Task<User?> GetById(string id)
    {
        return await _context.Users.Find(x => x.Id == id).FirstOrDefaultAsync();
    }

    public async Task<User> Create(User user)
    {
        await _context.Users.InsertOneAsync(user);
        return user;
    }

    public async Task<User> Update(User user)
    {
        await _context.Users.ReplaceOneAsync(x => x.Id == user.Id, user);
        return user;
    }

    public async Task<TempToken?> GetToken(string token)
    {
        return await _context.TempTokens.Find(x => x.Token == token).FirstOrDefaultAsync();
    }

    public async Task<TempToken> CreateToken(TempToken token)
    {
        await _context.TempTokens.InsertOneAsync(token);
        return token;
    }
}