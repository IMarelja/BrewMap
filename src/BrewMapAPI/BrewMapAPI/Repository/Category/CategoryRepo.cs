using BrewMapAPI.Data;
using BrewMapAPI.DTO.Category;
using BrewMapAPI.Models;
using MongoDB.Driver;

namespace BrewMapAPI.Repository.Categories
{
    public class CategoryRepo : ICategoryRepo
    {
        private readonly IMongoCollection<Category> _categories;

        public CategoryRepo(MongoDbContext context)
        {
            _categories = context.Categories;
        }

        public async Task<List<Category>> GetAllAsync()
        {
            return await _categories.Find(_ => true).ToListAsync();
        }

        public async Task<Category?> GetByTagAsync(string tag)
        {
            return await _categories
                .Find(c => c.Tag == tag.ToLower())
                .FirstOrDefaultAsync();
        }

        public async Task<Category> CreateAsync(CreateCategory dto)
        {
            var normalizedTag = dto.Tag.ToLower();

            var category = new Category
            {
                Tag = normalizedTag,
                Name = dto.Name,
                IsActive = true,
                CreatedAt = DateTime.UtcNow,
                UpdatedAt = DateTime.UtcNow
            };

            await _categories.InsertOneAsync(category);
            return category;
        }

        public async Task<Category?> EditAsync(string tag, EditCategory dto)
        {
            var normalizedTag = tag.ToLower();

            var update = Builders<Category>.Update
                .Set(c => c.Name, dto.Name)
                .Set(c => c.UpdatedAt, DateTime.UtcNow);

            var options = new FindOneAndUpdateOptions<Category>
            {
                ReturnDocument = ReturnDocument.After
            };

            return await _categories.FindOneAndUpdateAsync(
                c => c.Tag == normalizedTag,
                update,
                options
            );
        }

        public async Task<bool> DeleteAsync(string tag)
        {
            var normalizedTag = tag.ToLower();

            var result = await _categories.DeleteOneAsync(
                c => c.Tag == normalizedTag
            );

            return result.DeletedCount > 0;
        }
    }
}
