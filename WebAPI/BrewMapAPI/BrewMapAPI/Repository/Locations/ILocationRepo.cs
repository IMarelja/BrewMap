using BrewMapAPI.Models;

namespace BrewMapAPI.Repository.Locations
{
    public interface ILocationRepo
    {
        Task<Location> CreateAsync(Location location);
        Task<Location?> GetByIdAsync(string id);
        Task<IEnumerable<Location>> GetAllAsync();
        Task<IEnumerable<Location>> SearchAsync(string? query, double? minRating, List<string>? paymentOptionTags, double? centerLatitude, double? centerLongitude, double radiusMeters);
        Task UpdateAsync(Location location);
        Task DeleteAsync(string id);
    }
}
