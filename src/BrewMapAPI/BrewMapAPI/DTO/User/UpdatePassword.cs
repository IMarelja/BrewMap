using System.ComponentModel.DataAnnotations;

namespace BrewMapAPI.DTO.User
{
    public class UpdatePassword
    {
        [Required]
        public string CurrentPassword { get; set; } = string.Empty;

        [Required]
        [DataType(DataType.Password)]
        public string NewPassword { get; set; } = string.Empty;

        [Required]
        [DataType(DataType.Password)]
        [Compare(nameof(NewPassword), ErrorMessage = "Passwords do not match.")]
        public string ConfirmNewPassword { get; set; } = string.Empty;
    }
}
