using System.ComponentModel.DataAnnotations;

namespace BrewMapAPI.DTO.Flag
{
    public class UpdateFlagStatus
    {
        [Required]
        public string Id { get; set; } = string.Empty;

        [Required]
        public string Status { get; set; } = string.Empty; // "pending", "reviewed", "resolved"

        [Required]
        public string ResolvedByAdminId { get; set; } = string.Empty;

        public string? ResolutionNote { get; set; }
    }
}