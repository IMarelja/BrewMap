using BrewMapAPI.DTO.Drink;
using BrewMapAPI.Models;
using BrewMapAPI.Repository.Drinks;
using MongoDB.Driver;

namespace BrewMapAPI.Service.Drinks
{
    public class DrinkService : IDrinkService
    {
        private readonly IDrinkRepo _repo;

        public DrinkService(IDrinkRepo repo)
        {
            _repo = repo;
        }

        public async Task<ReadDrink> CreateDrink(CreateDrink drink, string userId)
        {
            drink.Name = (drink.Name ?? string.Empty).Trim();
            drink.Description = drink.Description?.Trim();

            try
            {
                var created = await _repo.CreateDrink(drink, userId);
                return toReadModel(created);
            }
            catch (MongoWriteException ex) when (ex.WriteError.Category == ServerErrorCategory.DuplicateKey)
            {
                throw new InvalidOperationException("A drink with the same name already exists at this location.");
            }
        }

        public async Task<bool> DeleteDrink(string id)
        {
            return await _repo.DeleteDrink(id);
        }

        public async Task<ReadDrink?> GetById(string id)
        {
            var drink = await _repo.GetById(id);
            if (drink == null)
                return null;
            return toReadModel(drink);
        }

        public async Task<List<ReadDrink>> GetByLocationId(string locationId)
        {
            var drinks = await _repo.GetByLocationId(locationId);
            return drinks.Select(toReadModel).ToList();
        }

        public async Task<ReadDrink?> UpdateDrink(string id, UpdateDrink drink)
        {
            drink.Name = drink.Name?.Trim();
            if (drink.Name != null && drink.Name.Length == 0)
                throw new ArgumentException("Name must not be empty.");
            drink.Description = drink.Description?.Trim();

            try
            {
                var updated = await _repo.UpdateDrink(id, drink);
                if (updated == null)
                    return null;
                return toReadModel(updated);
            }
            catch (MongoWriteException ex) when (ex.WriteError.Category == ServerErrorCategory.DuplicateKey)
            {
                throw new InvalidOperationException("A drink with the same name already exists at this location.");
            }
        }

        public async Task<ReadBestDrink?> GetBestDrinkByLocationId(string locationId)
        {
            var drink = await _repo.GetBestDrinkByLocationId(locationId);
            if (drink == null)
                return null;
            return new ReadBestDrink
            {
                Id = drink.Id,
                Name = drink.Name,
                Rating = drink.AggregatedRating.Average
            };
        }

        private ReadDrink toReadModel(Drink drink)
        {
            return new ReadDrink
            {
                Id = drink.Id,
                Name = drink.Name,
                Description = drink.Description,
                AvailableAtLocationId = drink.AvailableAtLocationId,
                CreatedByUserId = drink.CreatedByUserId,
                CreatedAt = drink.CreatedAt,
                UpdatedAt = drink.UpdatedAt,
                AggregatedRating = new ReadAggregatedRating
                {
                    Average = drink.AggregatedRating.Average,
                    Count = drink.AggregatedRating.Count
                }
            };
        }
    }
}
