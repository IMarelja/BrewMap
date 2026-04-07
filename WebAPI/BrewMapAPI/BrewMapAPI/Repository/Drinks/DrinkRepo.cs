
using BrewMapAPI.Data;
using BrewMapAPI.DTO.Drink;
using BrewMapAPI.Models;
using MongoDB.Driver;

namespace BrewMapAPI.Repository.Drinks
{
    public class DrinkRepo : IDrinkRepo
    {
        private readonly MongoDbContext _context;

        public DrinkRepo(MongoDbContext context)
        {
            _context = context;
        }

        public Task<Drink> CreateDrink(CreateDrink drink)
        {
            throw new NotImplementedException();
        }

        public Task<bool> DeleteDrink(string id)
        {
            throw new NotImplementedException();
        }

        public async Task<Drink?> GetById(string id)
        {
            return await _context.Drinks.Find(x => x.Id == id).FirstOrDefaultAsync();
        }

        public Task<List<Drink>> GetByLocationId(string locationId)
        {
            throw new NotImplementedException();
        }

        public Task<Drink?> UpdateDrink(UpdateDrink drink)
        {
            throw new NotImplementedException();
        }
    }
}
