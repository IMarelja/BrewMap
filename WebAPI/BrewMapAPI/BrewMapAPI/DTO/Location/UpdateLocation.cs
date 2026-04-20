using BrewMapAPI.Models;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;

namespace BrewMapAPI.DTO.Location
{
    public class UpdateLocation : IValidatableObject
    {

        // MUST STAY LOWERCASE
        private static readonly string[] RequiredDays =
        {
            "monday", "tuesday", "wednesday",
            "thursday", "friday", "saturday", "sunday"
        };

        public string Name { get; set; }
        public string? Description { get; set; }
        public Address Address { get; set; }
        public double? Latitude { get; set; }
        public double? Longitude { get; set; }
        public string CategoryTag { get; set; }
        public List<string> PaymentOptionTags { get; set; }
        public Contact? Contact { get; set; }
        public string? EditComment { get; set; }

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
    }
}