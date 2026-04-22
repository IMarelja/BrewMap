using System.ComponentModel.DataAnnotations;

namespace BrewMapAPI.DTO.Flag
{
    public class CreateFlag
    {
        [Required]
        public string ReportedByUserId { get; set; } = string.Empty;

        [Required]
        public ReportTargetDto Target { get; set; }

        [Required]
        [MinLength(3, ErrorMessage = "Reason must be at least 3 characters")]
        public string Reason { get; set; } = string.Empty;

        public string? Description { get; set; }
    }

    public class ReportTargetDto
    {
        [Required]
        public string Type { get; set; } = string.Empty; // "location", "product", "review"

        [Required]
        public string Id { get; set; } = string.Empty;
    }
}