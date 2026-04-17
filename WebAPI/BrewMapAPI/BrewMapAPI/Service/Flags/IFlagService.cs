using BrewMapAPI.DTO.Flag;
using BrewMapAPI.Models;

namespace BrewMapAPI.Service.Flags
{
    public interface IFlagService
    {
        Task<ReadFlag> CreateFlag(CreateFlag flag);
        Task<ReadFlag?> GetById(string id);
        Task<List<ReadFlag>> GetAll(FlagStatus? status = null, ContentType? contentType = null);
        Task<ReadFlag?> UpdateStatus(UpdateFlagStatus dto);
        Task<Dictionary<string, int>> GetStatistics();
    }
}