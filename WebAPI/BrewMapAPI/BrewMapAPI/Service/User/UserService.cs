using BrewMapAPI.Models;
using Microsoft.Extensions.Options;
using MongoDB.Driver;

namespace BrewMapAPI.Service.User
{
    public class UserService
    {
        private readonly IMongoCollection<Models.User> _users;

        public UserService(IOptions<DatabaseSettings> dbSettings)
        {
            var client = new MongoClient(dbSettings.Value.ConnectionString);
            var database = client.GetDatabase(dbSettings.Value.DatabaseName);
            _users = database.GetCollection<Models.User>("users");
        }

   //Get all users
        public async Task<List<Models.User>> GetAsync() =>
            await _users.Find(_ => true).ToListAsync();

//Get a single user by Id 
        public async Task<Models.User?> GetByIdAsync(string id) =>
            await _users.Find(u => u.Id == id).FirstOrDefaultAsync();
       
    }
}