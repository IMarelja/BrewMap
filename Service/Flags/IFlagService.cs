using BrewMapAPI.DTO.Flag;

namespace BrewMapAPI.Service.Flags
{
    public interface IFlagService
    {
        Task<ReadFlag> CreateFlag(CreateFlag flag, string userId);
        Task<ReadFlag?> GetById(string id);
        Task<List<ReadFlag>> GetAll(string? status = null, string? targetType = null);
        Task<ReadFlag?> UpdateStatus(UpdateFlagStatus dto);
        Task<Dictionary<string, int>> GetStatistics();
    }
}