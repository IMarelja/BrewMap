using BrewMapAPI.DTO.Location;
using BrewMapAPI.Models;
using BrewMapAPI.Repository;
using BrewMapAPI.Repository.Categories;
using BrewMapAPI.Repository.Drinks;
using BrewMapAPI.Repository.Locations;
using BrewMapAPI.Repository.PaymentOptions;
using MongoDB.Driver.GeoJsonObjectModel;

namespace BrewMapAPI.Service.Location
{
    public class LocationService: ILocationService
    {
        private readonly ILocationRepo _repo;
        private readonly ICategoryRepo _categoryRepo;
        private readonly IPaymentOptionRepo _paymentOptionRepo;
        private readonly IDrinkRepo _drinkRepo;

        public LocationService(
            ILocationRepo repo,
            ICategoryRepo categoryRepo,
            IPaymentOptionRepo paymentOptionRepo,
            IDrinkRepo drinkRepo)
        {
            _repo = repo;
            _categoryRepo = categoryRepo;
            _paymentOptionRepo = paymentOptionRepo;
            _drinkRepo = drinkRepo;
        }

        public async Task<ReadLocation> CreateAsync(CreateLocation dto, string userId)
        {
            await ValidateTagsAsync(dto.CategoryTag, dto.PaymentOptionTags);

            var location = new Models.Location
            {
                Name = dto.Name,
                Description = dto.Description,
                Address = dto.Address,
                CategoryTag = dto.CategoryTag,
                PaymentOptionTags = dto.PaymentOptionTags,
                OpeningHours = dto.OpeningHours,
                Contact = dto.Contact == null ? null : new Contact
                {
                    Website = dto.Contact.Website
                },
                AddedByUserId = userId,
                LocationPoint = GeoJson.Point(GeoJson.Geographic(dto.Longitude, dto.Latitude)),
                IsActive = true
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

            await ValidateTagsAsync(dto.CategoryTag, dto.PaymentOptionTags);

            location.Name = dto.Name;
            location.Address = dto.Address;
            location.CategoryTag = dto.CategoryTag;
            location.PaymentOptionTags = dto.PaymentOptionTags;

            if (dto.Description != null)
                location.Description = dto.Description;

            if (IsValidOpeningHours(dto.OpeningHours))
                location.OpeningHours = dto.OpeningHours!;

            if (dto.Contact != null)
            {
                location.Contact ??= new Contact();
                location.Contact.Website = dto.Contact.Website;
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

        public async Task<IEnumerable<ReadLocation>> SearchAsync(
            string? query, double? minRating, string? drinkType, List<string>? paymentOptionTags, double? centerLatitude, double? centerLongitude, double radiusMeters)
        {
            var locations = (await _repo.SearchAsync(query, minRating, paymentOptionTags, centerLatitude, centerLongitude, radiusMeters)).ToList();

            if (!string.IsNullOrWhiteSpace(drinkType))
            {
                var dt = drinkType.Trim();
                var locationIds = new HashSet<string>(StringComparer.OrdinalIgnoreCase);

                foreach (var loc in locations)
                {
                    var drinks = await _drinkRepo.GetByLocationId(loc.Id);
                    if (drinks.Any(d => d.IsVisible && !string.IsNullOrWhiteSpace(d.Name) && d.Name.Contains(dt, StringComparison.OrdinalIgnoreCase)))
                        locationIds.Add(loc.Id);
                }

                locations = locations.Where(l => locationIds.Contains(l.Id)).ToList();
            }

            return locations.Select(MapToReadDto);
        }

        private static ReadLocation MapToReadDto(Models.Location location)
        {
            return new ReadLocation
            {
                Id = location.Id,
                Name = location.Name,
                Description = location.Description,
                Address = location.Address,
                Latitude = location.LocationPoint?.Coordinates?.Latitude ?? 0,
                Longitude = location.LocationPoint?.Coordinates?.Longitude ?? 0,
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

            var category = await _categoryRepo.GetByTagAsync(categoryTag);
            if (category == null || !category.IsActive)
                throw new ArgumentException($"Invalid CategoryTag: '{categoryTag}'.");

            if (paymentTags == null || !paymentTags.Any())
                return;

            var invalidTags = new List<string>();

            foreach (var tag in paymentTags.Distinct(StringComparer.OrdinalIgnoreCase))
            {
                var paymentOption = await _paymentOptionRepo.GetByTagAsync(tag);
                if (paymentOption == null || !paymentOption.IsActive)
                    invalidTags.Add(tag);
            }

            if (invalidTags.Any())
                throw new ArgumentException(
                    $"Invalid PaymentOptionTags: {string.Join(", ", invalidTags)}.");
        }

private static readonly string[] RequiredDays =
{
    "monday", "tuesday", "wednesday",
    "thursday", "friday", "saturday", "sunday"
};

        private bool IsValidOpeningHours(Dictionary<string, DayOpeningHours>? hours)
        {
            if (hours == null)
                return false;

            if (!RequiredDays.All(d => hours.ContainsKey(d)))
                return false;

            if (hours.Keys.Any(k => !RequiredDays.Contains(k)))
                return false;

            foreach (var day in hours.Values)
            {
                if (day == null) return false;

                if (day.IsClosed)
                {
                    if (!string.IsNullOrWhiteSpace(day.Open) ||
                        !string.IsNullOrWhiteSpace(day.Close))
                        return false;
                }
                else
                {
                    if (string.IsNullOrWhiteSpace(day.Open) ||
                        string.IsNullOrWhiteSpace(day.Close))
                        return false;
                }
            }

            return true;
        }

    }
}

