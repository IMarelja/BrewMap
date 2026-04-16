using BrewMapAPI.Models;

namespace BrewMapAPI.Repository.Reviews
{
    public interface IReviewRepo
    {
        Task<Review?> GetById(string id);
        Task<List<Review>> GetByTarget(string targetType, string targetId); // Both needed to uniquely identify what is reviewed
        Task<List<Review>> GetByUserId(string userId);
        Task Create(Review review);
        Task Update(Review review);
        Task<bool> Delete(string id);
    }
}