using BrewMapAPI.Data;
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

        public async Task<Category?> GetByTagAsync(string tag)
        {
            var filter = Builders<Category>.Filter.Regex(
                    c => c.Tag,
                    new MongoDB.Bson.BsonRegularExpression($"^{tag}$", "i"));

            return await _categories.Find(filter).FirstOrDefaultAsync();
        }
    }
}
