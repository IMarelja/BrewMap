using BrewMapAPI.Data;
using BrewMapAPI.Models;
using MongoDB.Driver;

namespace BrewMapAPI.Repository.Reviews
{
    public class ReviewRepo : IReviewRepo
    {
        private readonly MongoDbContext _context;

        public ReviewRepo(MongoDbContext context)
        {
            _context = context;
        }

        public async Task<Review?> GetById(string id)
        {
            return await _context.Reviews.Find(x => x.Id == id).FirstOrDefaultAsync();
        }

        public async Task<List<Review>> GetByTarget(string targetType, string targetId)
        {
            var filter = Builders<Review>.Filter.Eq(x => x.Target.Type, targetType) &
                         Builders<Review>.Filter.Eq(x => x.Target.TargetId, targetId);
            return await _context.Reviews.Find(filter).ToListAsync();
        }

        public async Task<List<Review>> GetByUserId(string userId)
        {
            return await _context.Reviews.Find(x => x.UserId == userId).ToListAsync();
        }

        public async Task Create(Review review)
        {
            await _context.Reviews.InsertOneAsync(review);
        }

        public async Task Update(Review review)
        {
            var filter = Builders<Review>.Filter.Eq(x => x.Id, review.Id);
            await _context.Reviews.ReplaceOneAsync(filter, review);
        }

        public async Task<bool> Delete(string id)
        {
            var result = await _context.Reviews.DeleteOneAsync(x => x.Id == id);
            return result.DeletedCount > 0;
        }
    }
}