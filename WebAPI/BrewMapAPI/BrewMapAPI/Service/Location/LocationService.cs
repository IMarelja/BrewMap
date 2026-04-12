using BrewMapAPI.DTO.Location;
using BrewMapAPI.Models;
using BrewMapAPI.Repository.Locations;

namespace BrewMapAPI.Service.Location
{
    public class LocationService: ILocationService
    {
        private readonly ILocationRepo _repo;

        public LocationService(ILocationRepo repo)
        {
            _repo = repo;
        }

        public async Task<ReadLocation> CreateAsync(CreateLocation dto, string userId)
        {
            var location = new Models.Location
            {
                Name = dto.Name,
                Description = dto.Description,
                Address = dto.Address,
                CategoryTag = dto.CategoryTag,
                PaymentOptionTags = dto.PaymentOptionTags,
                OpeningHours = dto.OpeningHours,
                Contact = dto.Contact,
                AddedByUserId = userId,
                LocationPoint = new GeoJsonPoint
                {
                    Coordinates = new List<double> { dto.Longitude, dto.Latitude }
                }
            };

            await _repo.CreateAsync(location);
            return MapToReadDto(location);
        }

        public async Task<IEnumerable<ReadLocation>> GetAllAsync()
        {
            var locations = await _repo.GetAllAsync();
            return locations.Select(MapToReadDto);
        }

        public async Task<ReadLocation?> GetByIdAsync(string id)
        {
            var location = await _repo.GetByIdAsync(id);
            return location == null ? null : MapToReadDto(location);
        }

        public async Task<bool> UpdateAsync(UpdateLocation dto, string userId)
        {
            var location = await _repo.GetByIdAsync(dto.Id);
            if (location == null) return false;

            if (!string.IsNullOrEmpty(dto.Name))
                location.Name = dto.Name;

            location.Description = dto.Description ?? location.Description;
            location.Address = dto.Address ?? location.Address;
            location.CategoryTag = dto.CategoryTag ?? location.CategoryTag;
            location.PaymentOptionTags = dto.PaymentOptionTags ?? location.PaymentOptionTags;
            location.OpeningHours = dto.OpeningHours ?? location.OpeningHours;
            location.Contact = dto.Contact ?? location.Contact;

            if (dto.Latitude.HasValue && dto.Longitude.HasValue)
            {
                location.LocationPoint = new GeoJsonPoint
                {
                    Coordinates = new List<double> { dto.Longitude.Value, dto.Latitude.Value }
                };
            }

            location.LastEdit = DateTime.UtcNow;
            location.UpdatedAt = DateTime.UtcNow;
            location.Edits.Add(new EditHistory
            {
                EditedByUserId = userId,
                EditedAt = DateTime.UtcNow,
                EditComment = dto.EditComment
            });

            await _repo.UpdateAsync(location);
            return true;
        }

        public async Task<bool> DeleteAsync(string id, string userId)
        {
            var location = await _repo.GetByIdAsync(id);
            if (location == null) return false;

            await _repo.DeleteAsync(id);
            return true;
        }

        private static ReadLocation MapToReadDto(Models.Location location)
        {
            return new ReadLocation
            {
                Id = location.Id,
                Name = location.Name,
                Description = location.Description,
                Address = location.Address,
                Latitude = location.LocationPoint?.Coordinates?.Count > 1
                        ? location.LocationPoint.Coordinates[1]
                        : 0,
                Longitude = location.LocationPoint?.Coordinates?.Count > 0
                        ? location.LocationPoint.Coordinates[0]
                        : 0,
                CategoryTag = location.CategoryTag,
                PaymentOptionTags = location.PaymentOptionTags,
                OpeningHours = location.OpeningHours,
                Contact = location.Contact,
                IsActive = location.IsActive,
                AverageRating = location.AggregatedRating?.Average ?? 0,
                TotalReviews = location.AggregatedRating?.Count ?? 0,
                CreatedAt = location.CreatedAt
            };
        }
    }
}

