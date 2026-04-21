using BrewMapAPI.Models;

namespace BrewMapAPI.Repository.Categories
{
    public interface ICategoryRepo
    {
        Task<Category?> GetByTagAsync(string tag);
    }
}
