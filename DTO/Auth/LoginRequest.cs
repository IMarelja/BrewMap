using System.ComponentModel.DataAnnotations;

namespace BrewMapAPI.DTO.Auth
{
    public class LoginRequest
    {
        [Required]
        public string User { get; set; } = string.Empty;

        [Required]
        [DataType(DataType.Password)]
        public string Password { get; set; } = string.Empty;
        
        public bool RememberMe { get; set; } = false;
    }
}
