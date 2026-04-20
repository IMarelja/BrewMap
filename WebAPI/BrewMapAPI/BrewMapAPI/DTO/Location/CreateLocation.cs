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
        public string Name { get; set; }
        public string? Description { get; set; }

        [Required]
        public Address Address { get; set; }

        [Required]
        public double Latitude { get; set; }

        [Required]
        public double Longitude { get; set; }
        [Required]
        public string CategoryTag { get; set; } = "cafe";
        [Required]
        public List<string> PaymentOptionTags { get; set; } = new();
        public ContactDto? Contact { get; set; }

        [Required]
        public Dictionary<string, DayOpeningHours> OpeningHours { get; set; }

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

        public class ContactDto
        {
            [OptionalUrl]
            public string Website { get; set; } = string.Empty;
        }

        public class OptionalUrlAttribute : ValidationAttribute
        {
            public override bool IsValid(object? value)
            {
                if (value == null || string.IsNullOrWhiteSpace(value as string))
                    return true;
                return new UrlAttribute().IsValid(value);
            }
        }
    }
}