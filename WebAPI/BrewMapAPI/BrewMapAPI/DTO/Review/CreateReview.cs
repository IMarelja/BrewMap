using System.ComponentModel.DataAnnotations;
using BrewMapAPI.Validation;
namespace BrewMapAPI.DTO.Review
{
     public class CreateReview
    {
        [Required]
        [TargetTypeValidation] // Custom validation attribute, see below
        public string TargetType { get; set; } = string.Empty; // Must be "location" or "product"

        [Required]
        public string TargetId { get; set; } = string.Empty;

        [Required]
        [Range(1, 5, ErrorMessage = "Rating must be between 1 and 5.")]
        public int Rating { get; set; }

        public string? Comment { get; set; }
    }
}