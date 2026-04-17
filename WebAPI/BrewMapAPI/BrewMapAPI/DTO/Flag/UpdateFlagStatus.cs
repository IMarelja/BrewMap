using System.ComponentModel.DataAnnotations;
using BrewMapAPI.Models;

namespace BrewMapAPI.DTO.Flag
{
    public class UpdateFlagStatus
    {
        [Required]
        public string Id { get; set; } = string.Empty;

        [Required]
        public FlagStatus Status { get; set; }

        [Required]
        public string ReviewedByUserId { get; set; } = string.Empty;
    }
}