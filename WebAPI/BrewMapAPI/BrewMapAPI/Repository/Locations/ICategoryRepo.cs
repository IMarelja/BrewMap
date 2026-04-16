using BrewMapAPI.Models;

namespace BrewMapAPI.Repository.Locations
{
    public interface ICategoryRepo
    {
        Task<Category?> GetByTagAsync(string tag);
    }
}
