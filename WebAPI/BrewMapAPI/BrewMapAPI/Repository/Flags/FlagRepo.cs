using BrewMapAPI.Data;
using BrewMapAPI.DTO.Flag;
using BrewMapAPI.Models;
using MongoDB.Driver;

namespace BrewMapAPI.Repository.Flags
{
    public class FlagRepo : IFlagRepo
    {
        private readonly MongoDbContext _context;

        public FlagRepo(MongoDbContext context)
        {
            _context = context;
        }

        public async Task<Flag> CreateFlag(CreateFlag dto)
        {
            var flag = new Flag
            {
                ReportedByUserId = dto.ReportedByUserId,
                ContentType = dto.ContentType,
                ContentId = dto.ContentId,
                Reason = dto.Reason,
                Status = FlagStatus.Pending,
                CreatedAt = DateTime.UtcNow,
                ContentSnapshot = new ContentSnapshot()
            };

            await _context.Flags.InsertOneAsync(flag);
            return flag;
        }

        public async Task<Flag?> GetById(string id)
        {
            return await _context.Flags.Find(x => x.Id == id).FirstOrDefaultAsync();
        }

        public async Task<List<Flag>> GetAll(FlagStatus? status = null, ContentType? contentType = null)
        {
            var filterBuilder = Builders<Flag>.Filter;
            var filters = new List<FilterDefinition<Flag>>();

            if (status.HasValue)
                filters.Add(filterBuilder.Eq(x => x.Status, status.Value));

            if (contentType.HasValue)
                filters.Add(filterBuilder.Eq(x => x.ContentType, contentType.Value));

            var finalFilter = filters.Count > 0 
                ? filterBuilder.And(filters) 
                : filterBuilder.Empty;

            return await _context.Flags
                .Find(finalFilter)
                .SortByDescending(x => x.CreatedAt)
                .ToListAsync();
        }

        public async Task<Flag?> UpdateStatus(UpdateFlagStatus dto)
        {
            var update = Builders<Flag>.Update
                .Set(x => x.Status, dto.Status)
                .Set(x => x.ReviewedAt, DateTime.UtcNow)
                .Set(x => x.ReviewedByUserId, dto.ReviewedByUserId);

            var options = new FindOneAndUpdateOptions<Flag> { ReturnDocument = ReturnDocument.After };
            
            return await _context.Flags.FindOneAndUpdateAsync(
                x => x.Id == dto.Id, 
                update, 
                options
            );
        }

        public async Task<Flag?> GetByUserAndContent(string userId, string contentId, ContentType contentType)
        {
            return await _context.Flags
                .Find(x => x.ReportedByUserId == userId 
                    && x.ContentId == contentId 
                    && x.ContentType == contentType)
                .FirstOrDefaultAsync();
        }

        public async Task<Dictionary<string, int>> GetStatistics()
        {
            var allFlags = await _context.Flags.Find(_ => true).ToListAsync();

            var stats = new Dictionary<string, int>
            {
                ["TotalFlags"] = allFlags.Count,
                ["PendingFlags"] = allFlags.Count(f => f.Status == FlagStatus.Pending),
                ["ReviewedFlags"] = allFlags.Count(f => f.Status == FlagStatus.Reviewed),
                ["ResolvedFlags"] = allFlags.Count(f => f.Status == FlagStatus.Resolved),
                ["LocationFlags"] = allFlags.Count(f => f.ContentType == ContentType.Location),
                ["DrinkFlags"] = allFlags.Count(f => f.ContentType == ContentType.Drink),
                ["ReviewFlags"] = allFlags.Count(f => f.ContentType == ContentType.Review)
            };

            return stats;
        }
    }
}