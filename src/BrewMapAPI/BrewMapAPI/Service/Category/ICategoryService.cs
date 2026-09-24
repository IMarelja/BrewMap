using BrewMapAPI.DTO.Category;

namespace BrewMapAPI.Service.Category
{
    public interface ICategoryService
    {
        Task<List<ReadCategory>> GetAllAsync();
        Task<ReadCategory?> GetByTagAsync(string tag);
        Task<ReadCategory> CreateAsync(CreateCategory dto);
        Task<ReadCategory?> EditAsync(string tag, EditCategory dto);
        Task<bool> DeleteAsync(string tag);
    }
}
