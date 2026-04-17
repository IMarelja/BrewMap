using System.ComponentModel.DataAnnotations;
using BrewMapAPI.Models;

namespace BrewMapAPI.DTO.Flag
{
    public class CreateFlag
    {
        [Required]
        public string ReportedByUserId { get; set; } = string.Empty;

        [Required]
        public ContentType ContentType { get; set; }

        [Required]
        public string ContentId { get; set; } = string.Empty;

        [Required]
        [MinLength(10, ErrorMessage = "Reason must be at least 10 characters")]
        public string Reason { get; set; } = string.Empty;
    }
}