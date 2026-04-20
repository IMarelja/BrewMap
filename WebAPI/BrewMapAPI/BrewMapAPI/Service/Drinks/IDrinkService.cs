using BrewMapAPI.DTO.Drink;
using BrewMapAPI.Models;

namespace BrewMapAPI.Service.Drinks
{
    public interface IDrinkService
    {
        Task<ReadDrink?> GetById(string id);
        Task<List<ReadDrink>> GetByLocationId(string locationId);
        Task<ReadDrink> CreateDrink(CreateDrink drink, string userId);
        Task<ReadDrink?> UpdateDrink(string id, UpdateDrink drink);
        Task<bool> DeleteDrink(string id);
    }
}
