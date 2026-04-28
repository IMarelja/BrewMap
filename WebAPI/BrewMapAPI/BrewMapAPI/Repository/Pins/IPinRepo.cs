using BrewMapAPI.Models;

namespace BrewMapAPI.Repository.Pins
{
    public interface IPinRepo
    {
        Task<Pin?> GetById(string id);
        Task<List<Pin>> GetByRange(double minLat, double maxLat, double minLon, double maxLon);
    }
}