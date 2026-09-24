using BrewMapAPI.DTO.Category;
using BrewMapAPI.Repository.Categories;

namespace BrewMapAPI.Service.Category
{
    public class CategoryService : ICategoryService
    {
        private readonly ICategoryRepo _repo;

        public CategoryService(ICategoryRepo repo)
        {
            _repo = repo;
        }

        public async Task<List<ReadCategory>> GetAllAsync()
        {
            var categories = await _repo.GetAllAsync();
            return categories.Select(ToReadDto).ToList();
        }

        public async Task<ReadCategory?> GetByTagAsync(string tag)
        {
            var category = await _repo.GetByTagAsync(tag);
            if (category == null)
                return null;

            return ToReadDto(category);
        }

        public async Task<ReadCategory> CreateAsync(CreateCategory dto)
        {
            dto.Tag = dto.Tag.Trim().ToLower();
            dto.Name = dto.Name.Trim();

            var existing = await _repo.GetByTagAsync(dto.Tag);
            if (existing != null)
                throw new InvalidOperationException($"A category with tag '{dto.Tag}' already exists.");

            var created = await _repo.CreateAsync(dto);
            return ToReadDto(created);
        }

        public async Task<ReadCategory?> EditAsync(string tag, EditCategory dto)
        {
            dto.Name = dto.Name.Trim();

            var updated = await _repo.EditAsync(tag, dto);
            if (updated == null)
                return null;

            return ToReadDto(updated);
        }

        public async Task<bool> DeleteAsync(string tag)
        {
            return await _repo.DeleteAsync(tag);
        }

        private ReadCategory ToReadDto(Models.Category model)
        {
            return new ReadCategory
            {
                Tag = model.Tag,
                Name = model.Name
            };
        }
    }
}
