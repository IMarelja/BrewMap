using System.ComponentModel.DataAnnotations;

namespace BrewMapAPI.DTO.Flag
{
    public class UpdateFlagStatus
    {
        [Required]
        public string Id { get; set; } = String.Empty;

        [Required]
        [AllowedValues("pending", "reviewed", "resolved")]
        public string Status { get; set; } = String.Empty;

        [Required]
        public string ResolvedByAdminId { get; set; } = String.Empty;

        public string? ResolutionNote { get; set; }
    }
}