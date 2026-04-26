using BrewMapAPI.DTO.Review;
using BrewMapAPI.Models;
using BrewMapAPI.Repository.Drinks;
using BrewMapAPI.Repository.Locations;
using BrewMapAPI.Repository.Reviews;

namespace BrewMapAPI.Service.Review
{
    public class ReviewService : IReviewService
    {
        private readonly IReviewRepo _repo;
        private readonly ILocationRepo _locationRepo;
        private readonly IDrinkRepo _drinkRepo;

        public ReviewService(IReviewRepo repo, ILocationRepo locationRepo, IDrinkRepo drinkRepo)
        {
            _repo = repo;
            _locationRepo = locationRepo;
            _drinkRepo = drinkRepo;
        }

        public async Task<ReadReview?> GetById(string id)
        {
            var review = await _repo.GetById(id);
            return review == null ? null : ToReadModel(review);
        }

        public async Task<List<ReadReview>> GetByTarget(string targetType, string targetId)
        {
            var reviews = await _repo.GetByTarget(targetType, targetId);
            return reviews.Select(ToReadModel).ToList();
        }

        public async Task<List<ReadReview>> GetByUserId(string userId)
        {
            var reviews = await _repo.GetByUserId(userId);
            return reviews.Select(ToReadModel).ToList();
        }

        public async Task<ReadReview> CreateLocationReview(string locationId, CreateReviewBody dto, string userId)
        {
            var review = new Models.Review
            {
                UserId = userId,
                Target = new ReviewTarget { Type = "location", TargetId = locationId },
                Rating = dto.Rating,
                Comment = dto.Comment,
                IsVisible = true,
                ReportCount = 0,
                CreatedAt = DateTime.UtcNow,
                UpdatedAt = DateTime.UtcNow
            };
            await _repo.Create(review);
            await RefreshLocationRating(locationId);
            return ToReadModel(review);
        }

        public async Task<ReadReview> CreateDrinkReview(string drinkId, CreateReviewBody dto, string userId)
        {
            var review = new Models.Review
            {
                UserId = userId,
                Target = new ReviewTarget { Type = "product", TargetId = drinkId },
                Rating = dto.Rating,
                Comment = dto.Comment,
                IsVisible = true,
                ReportCount = 0,
                CreatedAt = DateTime.UtcNow,
                UpdatedAt = DateTime.UtcNow
            };
            await _repo.Create(review);
            await RefreshDrinkRating(drinkId);
            return ToReadModel(review);
        }

        public async Task<ReadReview?> UpdateReview(string Id, UpdateReview dto, string userId)
        {
            var review = await _repo.GetById(Id);
            if (review == null || review.UserId != userId)
                return null;

            if (dto.Rating.HasValue)
                review.Rating = dto.Rating.Value;
            if (dto.Comment != null)
                review.Comment = dto.Comment;
            review.UpdatedAt = DateTime.UtcNow;
            await _repo.Update(review);
            await RefreshTargetRating(review.Target.Type, review.Target.TargetId);
            return ToReadModel(review);
        }

        public async Task<bool> DeleteReview(string id, string userId)
        {
            var review = await _repo.GetById(id);
            if (review == null || review.UserId != userId)
                return false;

            var targetType = review.Target.Type;
            var targetId = review.Target.TargetId;

            var deleted = await _repo.Delete(id);
            if (deleted)
                await RefreshTargetRating(targetType, targetId);
            return deleted;
        }

        private async Task RefreshTargetRating(string targetType, string targetId)
        {
            if (targetType == "location")
                await RefreshLocationRating(targetId);
            else if (targetType == "product")
                await RefreshDrinkRating(targetId);
        }

        private async Task RefreshLocationRating(string locationId)
        {
            var (average, count) = await _repo.GetAggregatedRating("location", locationId);
            await _locationRepo.UpdateAggregatedRatingAsync(locationId, average, count);
        }

        private async Task RefreshDrinkRating(string drinkId)
        {
            var (average, count) = await _repo.GetAggregatedRating("product", drinkId);
            await _drinkRepo.UpdateAggregatedRating(drinkId, average, count);
        }

        private ReadReview ToReadModel(Models.Review review)
        {
            return new ReadReview
            {
                Id = review.Id,
                UserId = review.UserId,
                TargetType = review.Target.Type,
                TargetId = review.Target.TargetId,
                Rating = review.Rating,
                Comment = review.Comment,
                IsVisible = review.IsVisible,
                ReportCount = review.ReportCount,
                CreatedAt = review.CreatedAt,
                UpdatedAt = review.UpdatedAt
            };
        }
    }
}
