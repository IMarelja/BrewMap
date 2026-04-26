using BrewMapAPI.DTO.Review;

namespace BrewMapAPI.Service.Review
{
    public interface IReviewService
    {
        Task<ReadReview?> GetById(string id);
        Task<List<ReadReview>> GetByTarget(string targetType, string targetId);
        Task<List<ReadReview>> GetByUserId(string userId);
        Task<ReadReview> CreateLocationReview(string locationId, CreateReviewBody dto, string userId);
        Task<ReadReview> CreateDrinkReview(string drinkId, CreateReviewBody dto, string userId);
        Task<ReadReview?> UpdateReview(UpdateReview review, string userId);
        Task<bool> DeleteReview(string id, string userId);
    }
}