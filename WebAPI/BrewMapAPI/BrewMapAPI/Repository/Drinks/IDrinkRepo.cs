using BrewMapAPI.DTO.Drink;
using BrewMapAPI.Models;


namespace BrewMapAPI.Repository.Drinks
{
    public interface IDrinkRepo
    {
        Task<Drink?> GetById(string id);
        Task<List<Drink>> GetByLocationId(string locationId);
        Task<Drink> CreateDrink(CreateDrink drink, string userId);
        Task<Drink?> UpdateDrink(string id, UpdateDrink drink);
        Task<bool> DeleteDrink(string id);
        Task UpdateAggregatedRating(string id, double average, int count);
    }
}
