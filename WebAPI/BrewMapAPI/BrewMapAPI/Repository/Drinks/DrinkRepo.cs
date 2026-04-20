
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

        public async Task<Drink> CreateDrink(CreateDrink dto, string userId)
        {
            var drink = new Drink
            {
                Name = dto.Name,
                Description = dto.Description,
                AvailableAtLocationId = dto.LocationId,
                CreatedByUserId = userId,
                CreatedAt = DateTime.UtcNow,
                UpdatedAt = DateTime.UtcNow
            };
            await _context.Drinks.InsertOneAsync(drink);
            return drink;
        }

        public async Task<bool> DeleteDrink(string id)
        {
            var result = await _context.Drinks.DeleteOneAsync(x => x.Id == id);
            return result.DeletedCount > 0;
        }

        public async Task<Drink?> GetById(string id)
        {
            return await _context.Drinks.Find(x => x.Id == id).FirstOrDefaultAsync();
        }

        public async Task<List<Drink>> GetByLocationId(string locationId)
        {
            return await _context.Drinks.Find(x => x.AvailableAtLocationId == locationId).ToListAsync();
        }

        public async Task<Drink?> UpdateDrink(string id, UpdateDrink dto)
        {
            var updates = new List<UpdateDefinition<Drink>>();
            var builder = Builders<Drink>.Update;

            if (dto.Name != null)
                updates.Add(builder.Set(x => x.Name, dto.Name));
            if (dto.Description != null)
                updates.Add(builder.Set(x => x.Description, dto.Description));

            updates.Add(builder.Set(x => x.UpdatedAt, DateTime.UtcNow));

            var update = builder.Combine(updates);
            var options = new FindOneAndUpdateOptions<Drink> { ReturnDocument = ReturnDocument.After };
            return await _context.Drinks.FindOneAndUpdateAsync(x => x.Id == id, update, options);
        }
    }
}
