using BrewMapAPI.DTO.Location;
using BrewMapAPI.Models;
using BrewMapAPI.Repository.Drinks;
using BrewMapAPI.Repository.Locations;

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
                LocationPoint = new GeoJsonPoint
                {
                    Coordinates = new List<double> { dto.Longitude, dto.Latitude }
                },
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

            if (!IsSwaggerString(dto.CategoryTag) || !IsSwaggerList(dto.PaymentOptionTags))
            {
                await ValidateTagsAsync(
                    IsSwaggerString(dto.CategoryTag) ? location.CategoryTag : dto.CategoryTag!,
                    IsSwaggerList(dto.PaymentOptionTags) ? location.PaymentOptionTags : dto.PaymentOptionTags!
                );
            }

            if (!IsSwaggerString(dto.Name))
                location.Name = dto.Name!;

            if (dto.Description != null && dto.Description.Trim().ToLower() != "string")
                location.Description = dto.Description;

            if (dto.Address != null)
                location.Address = dto.Address;

            if (!IsSwaggerString(dto.CategoryTag))
                location.CategoryTag = dto.CategoryTag!;

            if (!IsSwaggerList(dto.PaymentOptionTags))
                location.PaymentOptionTags = dto.PaymentOptionTags!;

            if (IsValidOpeningHours(dto.OpeningHours))
            {
                location.OpeningHours = dto.OpeningHours!;
            }

            if (dto.Contact != null)
            {
                location.Contact ??= new Contact();

                if (!IsSwaggerString(dto.Contact.Website))
                    location.Contact.Website = dto.Contact.Website;
            }

            if (!IsSwaggerCoordinate(dto.Latitude) && !IsSwaggerCoordinate(dto.Longitude))
            {
                location.LocationPoint = new GeoJsonPoint
                {
                    Coordinates = new List<double>
            {
                dto.Longitude!.Value,
                dto.Latitude!.Value
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

        public async Task<IEnumerable<ReadLocation>> SearchAsync(
    string? query,
    double? minRating,
    string? drinkType,
    List<string>? paymentOptionTags,
    double? latitude,
    double? longitude,
    double radiusMeters)
        {
            var locations = (await _repo.GetAllAsync()).ToList();

            var filtered = locations
                .Where(l => l.IsActive)
                .ToList();

            if (!string.IsNullOrWhiteSpace(query))
            {
                var q = query.Trim().ToLower();

                filtered = filtered.Where(l =>
                    (!string.IsNullOrWhiteSpace(l.Name) &&
                        l.Name.ToLower().Contains(q)) ||

                    (!string.IsNullOrWhiteSpace(l.Address?.City) &&
                        l.Address.City.ToLower().Contains(q))
                ).ToList();
            }

            if (minRating.HasValue)
            {
                filtered = filtered.Where(l =>
                    l.AggregatedRating != null &&
                    l.AggregatedRating.Average >= minRating.Value
                ).ToList();
            }

            if (!string.IsNullOrWhiteSpace(drinkType))
            {
                var dt = drinkType.Trim().ToLower();
                var locationIds = new HashSet<string>();

                foreach (var loc in locations)
                {
                    var drinks = await _drinkRepo.GetByLocationId(loc.Id);

                    if (drinks.Any(d =>
                        d.IsVisible &&
                        !string.IsNullOrWhiteSpace(d.Name) &&
                        d.Name.ToLower().Contains(dt)))
                    {
                        locationIds.Add(loc.Id);
                    }
                }

                filtered = filtered
                    .Where(l => locationIds.Contains(l.Id))
                    .ToList();
            }

            if (paymentOptionTags != null && paymentOptionTags.Any())
            {
                filtered = filtered.Where(l =>
                    paymentOptionTags.All(p =>
                        l.PaymentOptionTags.Contains(p, StringComparer.OrdinalIgnoreCase)))
                    .ToList();
            }

            if (latitude.HasValue && longitude.HasValue)
            {
                filtered = filtered.Where(l =>
                {
                    var lat2 = l.LocationPoint.Coordinates[1];
                    var lon2 = l.LocationPoint.Coordinates[0];

                    var distance = Haversine(latitude.Value, longitude.Value, lat2, lon2);
                    return distance <= radiusMeters;
                }).ToList();
            }

            return filtered.Select(MapToReadDto);
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

        private bool IsSwaggerString(string? value)
        {
            return string.IsNullOrWhiteSpace(value) || value.Trim().ToLower() == "string";
        }

        private bool IsSwaggerList(List<string>? list)
        {
            return list == null || !list.Any() ||
                   list.All(x => x.Trim().ToLower() == "string");
        }

        private bool IsSwaggerCoordinate(double? value)
        {
            return !value.HasValue || value.Value == 0;
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

                    if (day.Open.ToLower() == "string" ||
                        day.Close.ToLower() == "string")
                        return false;
                }
            }

            return true;
        }

        private static double Haversine(double lat1, double lon1, double lat2, double lon2)
        {
            const double R = 6371000;

            var dLat = ToRad(lat2 - lat1);
            var dLon = ToRad(lon2 - lon1);

            var a =
                Math.Sin(dLat / 2) * Math.Sin(dLat / 2) +
                Math.Cos(ToRad(lat1)) * Math.Cos(ToRad(lat2)) *
                Math.Sin(dLon / 2) * Math.Sin(dLon / 2);

            var c = 2 * Math.Atan2(Math.Sqrt(a), Math.Sqrt(1 - a));

            return R * c;
        }

        private static double ToRad(double angle)
        {
            return Math.PI * angle / 180.0;
        }
    }
}

