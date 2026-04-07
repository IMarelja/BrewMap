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

        public Task<ReadDrink> CreateDrink(CreateDrink drink)
        {
            throw new NotImplementedException();
        }

        public Task<bool> DeleteDrink(string id)
        {
            throw new NotImplementedException();
        }

        public async Task<ReadDrink?> GetById(string id)
        {
            var drink = await _repo.GetById(id);
            if (drink == null)
                return null;
            return toReadModel(drink);
        }

        public Task<List<ReadDrink>> GetByLocationId(string locationId)
        {
            throw new NotImplementedException();
        }

        public Task<ReadDrink?> UpdateDrink(UpdateDrink drink)
        {
            throw new NotImplementedException();
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
