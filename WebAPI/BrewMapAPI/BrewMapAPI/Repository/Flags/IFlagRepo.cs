using BrewMapAPI.DTO.Flag;
using BrewMapAPI.Models;

namespace BrewMapAPI.Repository.Flags
{
    public interface IFlagRepo
    {
        Task<Flag> CreateFlag(CreateFlag flag);
        Task<Flag?> GetById(string id);
        Task<List<Flag>> GetAll(FlagStatus? status = null, ContentType? contentType = null);
        Task<Flag?> UpdateStatus(UpdateFlagStatus dto);
        Task<Flag?> GetByUserAndContent(string userId, string contentId, ContentType contentType);
        Task<Dictionary<string, int>> GetStatistics();
    }
}