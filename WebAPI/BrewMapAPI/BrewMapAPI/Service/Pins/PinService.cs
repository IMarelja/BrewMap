using BrewMapAPI.DTO.Pin;
using BrewMapAPI.Models;
using BrewMapAPI.Repository.Pins;

namespace BrewMapAPI.Service.Pins
{
    public class PinService : IPinService
    {
        private readonly IPinRepo _repo;

        public PinService(IPinRepo repo)
        {
            _repo = repo;
        }

        public async Task<ReadPin?> GetById(string id)
        {
            var pin = await _repo.GetById(id);
            if (pin == null)
                return null;
            return toReadModel(pin);
        }

        public async Task<List<ReadPin>> GetByRange(double minLat, double maxLat, double minLon, double maxLon)
        {
            var pins = await _repo.GetByRange(minLat, maxLat, minLon, maxLon);
            return pins.Select(toReadModel).ToList();
        }

        private ReadPin toReadModel(Pin pin)
        {
            return new ReadPin
            {
                Id = pin.Id,
                Latitude = pin.Latitude,
                Longitude = pin.Longitude
            };
        }
    }
}