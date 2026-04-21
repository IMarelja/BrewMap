using BrewMapAPI.Models;

namespace BrewMapAPI.Repository.Locations
{
    public interface ILocationRepo
    {
        Task<Location> CreateAsync(Location location);
        Task<Location?> GetByIdAsync(string id);
        Task<IEnumerable<Location>> GetAllAsync();
        Task UpdateAsync(Location location);
        Task DeleteAsync(string id);
    }
}
