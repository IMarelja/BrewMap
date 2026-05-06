using BrewMapAPI.DTO.Location;

namespace BrewMapAPI.Service.Location
{
    public interface ILocationService
    {
        Task<ReadLocation> CreateAsync(CreateLocation dto, string userId);
        Task<IEnumerable<ReadLocation>> GetAllAsync();
        Task<ReadLocation?> GetByIdAsync(string id);
        Task<bool> UpdateAsync(string id, UpdateLocation dto, string userId);
        Task<bool> DeleteAsync(string id, string userId);
        Task<IEnumerable<ReadLocation>> SearchAsync(string? query, double? minRating, string? drinkQuery, List<string>? categoryTags, List<string>? paymentOptionTags, double? centerLatitude, double? centerLongitude, double radiusMeters);
    }
}
