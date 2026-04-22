using BrewMapAPI.DTO.Flag;
using BrewMapAPI.Models;
using BrewMapAPI.Repository.Flags;

namespace BrewMapAPI.Service.Flags
{
    public class FlagService : IFlagService
    {
        private readonly IFlagRepo _repo;

        public FlagService(IFlagRepo repo)
        {
            _repo = repo;
        }

        public async Task<ReadFlag> CreateFlag(CreateFlag flag)
        {

            var created = await _repo.CreateFlag(flag);

            //WORK IN PROGRESS: Increment report count on target entity
            //await _repo.IncrementReportCount(flag.Target.Type, flag.Target.Id);

            return ToReadModel(created);
        }

        public async Task<ReadFlag?> GetById(string id)
        {
            var flag = await _repo.GetById(id);
            if (flag == null)
                return null;
            return ToReadModel(flag);
        }

        public async Task<List<ReadFlag>> GetAll(string? status = null, string? targetType = null)
        {
            var flags = await _repo.GetAll(status, targetType);
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

        private ReadFlag ToReadModel(Flag flag)
        {
            return new ReadFlag
            {
                Id = flag.Id,
                ReportedByUserId = flag.ReportedByUserId,
                Target = flag.Target,
                Reason = flag.Reason,
                Description = flag.Description,
                Status = flag.Status,
                ResolvedByAdminId = flag.ResolvedByAdminId,
                ResolvedAt = flag.ResolvedAt,
                ResolutionNote = flag.ResolutionNote,
                CreatedAt = flag.CreatedAt,
                UpdatedAt = flag.UpdatedAt
            };
        }
    }
}