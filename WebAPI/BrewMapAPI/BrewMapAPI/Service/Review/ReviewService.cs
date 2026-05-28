using BrewMapAPI.DTO.Review;
using BrewMapAPI.Models;
using BrewMapAPI.Repository.Drinks;
using BrewMapAPI.Repository.Locations;
using BrewMapAPI.Repository.Reviews;
using BrewMapAPI.Repository.Users;
using BrewMapAPI.Service.User;
using Microsoft.AspNetCore.Http.HttpResults;

namespace BrewMapAPI.Service.Review
{
    public class ReviewService : IReviewService
    {
        private readonly IReviewRepo _repo;
        private readonly ILocationRepo _locationRepo;
        private readonly IDrinkRepo _drinkRepo;
        private readonly IUserRepo _userRepo;

        public ReviewService(IReviewRepo repo, ILocationRepo locationRepo, IDrinkRepo drinkRepo, IUserRepo userRepo)
        {
            _repo = repo;
            _locationRepo = locationRepo;
            _drinkRepo = drinkRepo;
            _userRepo = userRepo;
        }

        public async Task<ReadReview?> GetById(string id)
        {
            var review = await _repo.GetById(id);
            return review == null ? null : await ToReadModel(review);
        }

        public async Task<List<ReadReview>> GetByTarget(string targetType, string targetId)
        {
            var reviews = await _repo.GetByTarget(targetType, targetId);
            var readModels = await Task.WhenAll(reviews.Select(ToReadModel));
            return readModels.ToList();
        }

        public async Task<List<ReadReview>> GetByUserId(string userId)
        {
            var reviews = await _repo.GetByUserId(userId);
            var readModels = await Task.WhenAll(reviews.Select(ToReadModel));
            return readModels.ToList();
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
            return await ToReadModel(review);
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
            return await ToReadModel(review);
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
            return await ToReadModel(review);
        }

        public async Task<bool> DeleteReview(string id, string userId)
        {
            var review = await _repo.GetById(id);

            if (review == null)
                return false;

            var isOwner = review.UserId == userId;
            if (!isOwner)
            {
                var user = await _userRepo.GetUserById(userId);
                var isAdmin = string.Equals(user?.Role, "admin", StringComparison.OrdinalIgnoreCase);

                if (!isAdmin)
                    return false;
            }

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

        private async Task<ReadReview> ToReadModel(Models.Review review)
        {
            var user = await _userRepo.GetUserById(review.UserId);

            return new ReadReview
            {
                Id = review.Id,
                UserId = review.UserId,
                Username = user?.Username,
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
