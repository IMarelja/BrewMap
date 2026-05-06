using System.ComponentModel.DataAnnotations;
namespace BrewMapAPI.DTO.Review
{
     public class CreateReview
    {
        [Required]
        [AllowedValues("location", "product")]
        public string TargetType { get; set; } = string.Empty;

        [Required]
        public string TargetId { get; set; } = string.Empty;

        [Required]
        [Range(1, 5, ErrorMessage = "Rating must be between 1 and 5.")]
        public int Rating { get; set; }

        public string? Comment { get; set; }
    }
}