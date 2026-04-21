using BrewMapAPI.Models;

namespace BrewMapAPI.Repository
{
    public interface ICategoryRepo
    {
        Task<Category?> GetByTagAsync(string tag);
    }
}
