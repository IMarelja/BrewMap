using System.ComponentModel.DataAnnotations;

namespace BrewMapAPI.DTO.Auth
{
    public class RegisterRequest
    {
        [Required]
        public string Email { get; set; } = string.Empty;
        
        [Required]
        public string Username { get; set; } = string.Empty;

        [Required]
        [DataType(DataType.Password)]
        public string Password { get; set; } = string.Empty;
    }
}
