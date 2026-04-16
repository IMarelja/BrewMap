using BrewMapAPI.DTO.Location;
using BrewMapAPI.Models;
using BrewMapAPI.Repository.Locations;

namespace BrewMapAPI.Service.Location
{
    public class LocationService: ILocationService
    {
        private readonly ILocationRepo _repo;
        private readonly ICategoryRepo _categoryRepo;
        private readonly IPaymentOptionRepo _paymentOptionRepo;

        public LocationService(
            ILocationRepo repo,
            ICategoryRepo categoryRepo,
            IPaymentOptionRepo paymentOptionRepo)
        {
            _repo = repo;
            _categoryRepo = categoryRepo;
            _paymentOptionRepo = paymentOptionRepo;
        }

        public async Task<ReadLocation> CreateAsync(CreateLocation dto, string userId)
        {
            //await ValidateTagsAsync(dto.CategoryTag, dto.PaymentOptionTags);

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

        public async Task<bool> UpdateAsync(string id, UpdateLocation dto, string userId)
        {
            var location = await _repo.GetByIdAsync(id);
            if (location == null) return false;

            if (!string.IsNullOrWhiteSpace(dto.CategoryTag) ||
                (dto.PaymentOptionTags != null && dto.PaymentOptionTags.Any()))
            {
                //await ValidateTagsAsync(
                //    dto.CategoryTag ?? location.CategoryTag,
                //    dto.PaymentOptionTags ?? location.PaymentOptionTags);
            }

            if (!string.IsNullOrWhiteSpace(dto.Name))
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
                    Coordinates = new List<double>
            {
                dto.Longitude.Value,
                dto.Latitude.Value
            }
                };
            }

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

        private async Task ValidateTagsAsync(string categoryTag, List<string> paymentTags)
        {
            if (string.IsNullOrWhiteSpace(categoryTag))
                throw new ArgumentException("CategoryTag is required.");

            //var category = await _categoryRepo.GetByTagAsync(categoryTag);
            //if (category == null)
            //    throw new ArgumentException($"Invalid CategoryTag: '{categoryTag}'.");

            if (paymentTags == null || !paymentTags.Any())
                return;

            var invalidTags = new List<string>();

            foreach (var tag in paymentTags.Distinct(StringComparer.OrdinalIgnoreCase))
            {
                var paymentOption = await _paymentOptionRepo.GetByTagAsync(tag);
                if (paymentOption == null)
                    invalidTags.Add(tag);
            }

            if (invalidTags.Any())
                throw new ArgumentException(
                    $"Invalid PaymentOptionTags: {string.Join(", ", invalidTags)}.");
        }
    }
}

