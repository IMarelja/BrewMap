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

        public async Task<Flag> CreateFlag(CreateFlag dto, string userId)
        {
            var flag = new Flag
            {
                ReportedByUserId = userId,
                Target = new ReportTarget
                {
                    Type = dto.Target.Type,
                    TargetId = dto.Target.Id
                },
                Reason = dto.Reason,
                Description = dto.Description,
                Status = "pending",
                CreatedAt = DateTime.UtcNow,
                UpdatedAt = DateTime.UtcNow
            };

            await _context.Flags.InsertOneAsync(flag);
            return flag;
        }

        public async Task<Flag?> GetById(string id)
        {
            return await _context.Flags.Find(x => x.Id == id).FirstOrDefaultAsync();
        }

        public async Task<List<Flag>> GetAll(string? status = null, string? targetType = null)
        {
            var filterBuilder = Builders<Flag>.Filter;
            var filters = new List<FilterDefinition<Flag>>();

            if (!string.IsNullOrEmpty(status))
                filters.Add(filterBuilder.Eq(x => x.Status, status));

            if (!string.IsNullOrEmpty(targetType))
                filters.Add(filterBuilder.Eq(x => x.Target.Type, targetType));

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
            var updates = new List<UpdateDefinition<Flag>>();
            var builder = Builders<Flag>.Update;

            updates.Add(builder.Set(x => x.Status, dto.Status));
            updates.Add(builder.Set(x => x.ResolvedByAdminId, dto.ResolvedByAdminId));
            updates.Add(builder.Set(x => x.ResolvedAt, DateTime.UtcNow));
            updates.Add(builder.Set(x => x.UpdatedAt, DateTime.UtcNow));
            
            if (!string.IsNullOrEmpty(dto.ResolutionNote))
                updates.Add(builder.Set(x => x.ResolutionNote, dto.ResolutionNote));

            var update = builder.Combine(updates);
            var options = new FindOneAndUpdateOptions<Flag> { ReturnDocument = ReturnDocument.After };
            
            return await _context.Flags.FindOneAndUpdateAsync(
                x => x.Id == dto.Id, 
                update, 
                options
            );
        }

        public async Task<Flag?> GetByUserAndTarget(string userId, string targetId, string targetType)
        {
            return await _context.Flags
                .Find(x => x.ReportedByUserId == userId 
                    && x.Target.TargetId == targetId 
                    && x.Target.Type == targetType)
                .FirstOrDefaultAsync();
        }

        public async Task<Dictionary<string, int>> GetStatistics()
        {
            var allFlags = await _context.Flags.Find(_ => true).ToListAsync();

            var stats = new Dictionary<string, int>
            {
                ["TotalFlags"] = allFlags.Count,
                ["PendingFlags"] = allFlags.Count(f => f.Status == "pending"),
                ["ReviewedFlags"] = allFlags.Count(f => f.Status == "reviewed"),
                ["ResolvedFlags"] = allFlags.Count(f => f.Status == "resolved"),
                ["LocationFlags"] = allFlags.Count(f => f.Target.Type == "location"),
                ["ProductFlags"] = allFlags.Count(f => f.Target.Type == "product"),
                ["ReviewFlags"] = allFlags.Count(f => f.Target.Type == "review")
            };

            return stats;
        }

        //WORK IN PROGRESS: Method to increment report count on target entity
        public async Task IncrementReportCount(string targetType, string targetId)
        {
            // - "location" -> LocationRepo.IncrementReportCount(targetId)
            // - "product" -> DrinkRepo.IncrementReportCount(targetId) 
            // - "review" -> ReviewRepo.IncrementReportCount(targetId)
            await Task.CompletedTask;
        }
    }
}