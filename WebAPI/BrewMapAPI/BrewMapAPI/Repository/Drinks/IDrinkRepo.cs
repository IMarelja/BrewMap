using BrewMapAPI.DTO.Drink;
using BrewMapAPI.Models;


namespace BrewMapAPI.Repository.Drinks
{
    public interface IDrinkRepo
    {
        Task<Drink?> GetById(string id);
        Task<List<Drink>> GetByLocationId(string locationId);
        Task<Drink> CreateDrink(CreateDrink drink);
        Task<Drink?> UpdateDrink(UpdateDrink drink);
        Task<bool> DeleteDrink(string id);
    }
}