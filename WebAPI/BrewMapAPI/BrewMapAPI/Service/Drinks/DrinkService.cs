using BrewMapAPI.DTO.Drink;
using BrewMapAPI.Models;
using BrewMapAPI.Repository.Drinks;

namespace BrewMapAPI.Service.Drinks
{
    public class DrinkService : IDrinkService
    {
        private readonly IDrinkRepo _repo;

        public DrinkService(IDrinkRepo repo)
        {
            _repo = repo;
        }

        public async Task<ReadDrink> CreateDrink(CreateDrink drink)
        {
            var created = await _repo.CreateDrink(drink);
            return toReadModel(created);
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

        public async Task<ReadDrink?> UpdateDrink(UpdateDrink drink)
        {
            drink.Name = (drink.Name ?? string.Empty).Trim();
            drink.Description = drink.Description?.Trim();

            var updated = await _repo.UpdateDrink(drink);
            if (updated == null)
                return null;
            return toReadModel(updated);
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
