using BrewMapAPI.DTO.Flag;
using BrewMapAPI.Models;
using BrewMapAPI.Repository.Flags;
using BrewMapAPI.Data;
using MongoDB.Driver;

namespace BrewMapAPI.Service.Flags
{
    public class FlagService : IFlagService
    {
        private readonly IFlagRepo _repo;
        private readonly MongoDbContext _context;

        public FlagService(IFlagRepo repo, MongoDbContext context)
        {
            _repo = repo;
            _context = context;
        }

        public async Task<ReadFlag> CreateFlag(CreateFlag flag)
        {
            var existingFlag = await _repo.GetByUserAndContent(
                flag.ReportedByUserId, 
                flag.ContentId, 
                flag.ContentType
            );

            if (existingFlag != null)
            {
                throw new InvalidOperationException("You have already reported this content.");
            }

            var created = await _repo.CreateFlag(flag);

            var snapshot = await CreateContentSnapshot(flag.ContentType, flag.ContentId);
            
            var update = Builders<Flag>.Update.Set(x => x.ContentSnapshot, snapshot);
            await _context.Flags.UpdateOneAsync(x => x.Id == created.Id, update);

            await IncrementReportCount(flag.ContentType, flag.ContentId);

            created.ContentSnapshot = snapshot;

            return ToReadModel(created);
        }

        public async Task<ReadFlag?> GetById(string id)
        {
            var flag = await _repo.GetById(id);
            if (flag == null)
                return null;
            return ToReadModel(flag);
        }

        public async Task<List<ReadFlag>> GetAll(FlagStatus? status = null, ContentType? contentType = null)
        {
            var flags = await _repo.GetAll(status, contentType);
            return flags.Select(ToReadModel).ToList();
        }

        public async Task<ReadFlag?> UpdateStatus(UpdateFlagStatus dto)
        {
            var updated = await _repo.UpdateStatus(dto);
            if (updated == null)
                return null;
            return ToReadModel(updated);
        }

        public async Task<Dictionary<string, int>> GetStatistics()
        {
            return await _repo.GetStatistics();
        }

        private async Task<ContentSnapshot> CreateContentSnapshot(ContentType contentType, string contentId)
        {
            var snapshot = new ContentSnapshot();

            switch (contentType)
            {
                case ContentType.Location:
                    var location = await _context.Locations.Find(x => x.Id == contentId).FirstOrDefaultAsync();
                    if (location != null)
                    {
                        snapshot.Name = location.Name;
                        snapshot.Description = location.Description;
                        snapshot.Address = $"{location.Address.Street}, {location.Address.City}, {location.Address.Country}";
                        snapshot.CreatedByUserId = location.AddedByUserId;
                    }
                    break;

                case ContentType.Drink:
                    var drink = await _context.Drinks.Find(x => x.Id == contentId).FirstOrDefaultAsync();
                    if (drink != null)
                    {
                        snapshot.Name = drink.Name;
                        snapshot.Description = drink.Description;
                        snapshot.CreatedByUserId = drink.CreatedByUserId;
                    }
                    break;

                case ContentType.Review:
                    var review = await _context.Reviews.Find(x => x.Id == contentId).FirstOrDefaultAsync();
                    if (review != null)
                    {
                        snapshot.Rating = review.Rating;
                        snapshot.Comment = review.Comment;
                        snapshot.CreatedByUserId = review.UserId;
                    }
                    break;
            }

            return snapshot;
        }

        private async Task IncrementReportCount(ContentType contentType, string contentId)
        {
            switch (contentType)
            {
                case ContentType.Location:
                    var locationUpdate = Builders<Location>.Update.Inc(x => x.ReportCount, 1);
                    await _context.Locations.UpdateOneAsync(x => x.Id == contentId, locationUpdate);
                    break;

                case ContentType.Drink:
                    var drinkUpdate = Builders<Drink>.Update.Inc(x => x.ReportCount, 1);
                    await _context.Drinks.UpdateOneAsync(x => x.Id == contentId, drinkUpdate);
                    break;

                case ContentType.Review:
                    var reviewUpdate = Builders<Review>.Update.Inc(x => x.ReportCount, 1);
                    await _context.Reviews.UpdateOneAsync(x => x.Id == contentId, reviewUpdate);
                    break;
            }
        }

        private ReadFlag ToReadModel(Flag flag)
        {
            return new ReadFlag
            {
                Id = flag.Id,
                ReportedByUserId = flag.ReportedByUserId,
                ContentType = flag.ContentType,
                ContentId = flag.ContentId,
                ContentSnapshot = flag.ContentSnapshot,
                Reason = flag.Reason,
                Status = flag.Status,
                CreatedAt = flag.CreatedAt,
                ReviewedAt = flag.ReviewedAt,
                ReviewedByUserId = flag.ReviewedByUserId
            };
        }
    }
}