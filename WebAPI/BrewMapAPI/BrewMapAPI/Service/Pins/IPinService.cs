using BrewMapAPI.DTO.Pin;

namespace BrewMapAPI.Service.Pins
{
    public interface IPinService
    {
        Task<ReadPin?> GetById(string id);
        Task<List<ReadPin>> GetByRange(double minLat, double maxLat, double minLon, double maxLon);
    }
}