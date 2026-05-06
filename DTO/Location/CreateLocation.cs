using BrewMapAPI.Attributes;
using BrewMapAPI.Models;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;

namespace BrewMapAPI.DTO.Location
{
    public class CreateLocation : IValidatableObject
    {
        private static readonly string[] RequiredDays =
        {
            "monday", "tuesday", "wednesday",
            "thursday", "friday", "saturday", "sunday"
        };

        [Required]
        public string Name { get; set; } = String.Empty;
        public string? Description { get; set; }

        [Required]
        public Address Address { get; set; } = new Address();

        [Required]
        public double Latitude { get; set; }

        [Required]
        public double Longitude { get; set; }
        [Required]
        public string CategoryTag { get; set; } = "cafe";
        [Required]
        public List<string> PaymentOptionTags { get; set; } = new();
        public CreateLocationContactDto? Contact { get; set; }

        [Required]
        public Dictionary<string, DayOpeningHours> OpeningHours { get; set; } = new Dictionary<string, DayOpeningHours>();

        public IEnumerable<ValidationResult> Validate(ValidationContext validationContext)
        {
            if (OpeningHours == null)
            {
                yield return new ValidationResult(
                    "OpeningHours is required.",
                    new[] { nameof(OpeningHours) });
                yield break;
            }

            foreach (var day in RequiredDays)
            {
                if (!OpeningHours.ContainsKey(day))
                {
                    yield return new ValidationResult(
                        $"OpeningHours must include '{day}'.",
                        new[] { nameof(OpeningHours) });
                }
            }
        }
    }

    public class CreateLocationContactDto
    {
        [OptionalUrl]
        public string? Website { get; set; }
    }
}