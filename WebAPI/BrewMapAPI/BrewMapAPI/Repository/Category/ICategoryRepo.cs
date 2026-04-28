using BrewMapAPI.DTO.Category;
using BrewMapAPI.Models;

namespace BrewMapAPI.Repository.Categories
{
    public interface ICategoryRepo
    {
        Task<Category?> GetByTagAsync(string tag);
        Task<List<Category>> GetAllAsync();
        Task<Category> CreateAsync(CreateCategory dto);
        Task<Category?> EditAsync(string tag, EditCategory dto);
        Task<bool> DeleteAsync(string tag);
    }
}
