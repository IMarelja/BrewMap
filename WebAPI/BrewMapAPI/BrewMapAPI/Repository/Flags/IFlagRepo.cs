using BrewMapAPI.DTO.Flag;
using BrewMapAPI.Models;

namespace BrewMapAPI.Repository.Flags
{
    public interface IFlagRepo
    {
        Task<Flag> CreateFlag(CreateFlag flag, string userId);
        Task<Flag?> GetById(string id);
        Task<List<Flag>> GetAll(string? status = null, string? targetType = null);
        Task<Flag?> UpdateStatus(UpdateFlagStatus dto);
        Task<Flag?> GetByUserAndTarget(string userId, string targetId, string targetType);
        Task<Dictionary<string, int>> GetStatistics();
    }
}