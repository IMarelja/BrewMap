using System.ComponentModel.DataAnnotations;

namespace BrewMapAPI.DTO.User
{
    public class UpdateEmail
    {
        [Required]
        [EmailAddress]
        public string NewEmail { get; set; } = string.Empty;

        [Required]
        public string CurrentPassword { get; set; } = string.Empty;
    }
}
