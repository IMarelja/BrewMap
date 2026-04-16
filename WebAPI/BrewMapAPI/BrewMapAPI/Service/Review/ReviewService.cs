using BrewMapAPI.DTO.Review;
using BrewMapAPI.Models;
using BrewMapAPI.Repository.Reviews;

namespace BrewMapAPI.Service.Review
{
    public class ReviewService : IReviewService
    {
        private readonly IReviewRepo _repo;

        public ReviewService(IReviewRepo repo)
        {
            _repo = repo;
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

        public async Task<ReadReview> CreateReview(CreateReview dto, string userId)
        {
            var review = new Models.Review
            {
                UserId = userId,
                Target = new ReviewTarget
                {
                    Type = dto.TargetType,
                    TargetId = dto.TargetId
                },
                Rating = dto.Rating,
                Comment = dto.Comment,
                IsVisible = true,
                ReportCount = 0,
                CreatedAt = DateTime.UtcNow,
                UpdatedAt = DateTime.UtcNow
            };
            await _repo.Create(review);
            return ToReadModel(review);
        }

        public async Task<ReadReview?> UpdateReview(UpdateReview dto, string userId)
        {
            var review = await _repo.GetById(dto.Id);
            if (review == null || review.UserId != userId)
                return null;

            if (dto.Rating.HasValue)
                review.Rating = dto.Rating.Value;
            if (dto.Comment != null)
                review.Comment = dto.Comment;
            if (dto.IsVisible.HasValue)
                review.IsVisible = dto.IsVisible.Value;

            review.UpdatedAt = DateTime.UtcNow;
            await _repo.Update(review);
            return ToReadModel(review);
        }

        public async Task<bool> DeleteReview(string id, string userId)
        {
            var review = await _repo.GetById(id);
            if (review == null || review.UserId != userId)
                return false;
            return await _repo.Delete(id);
        }

        // Helper function for mapping
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