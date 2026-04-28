using System.ComponentModel.DataAnnotations;

namespace BrewMapAPI.DTO.Auth;

public class ChangePasswordRequest
{
    [Required]
    public string ResetToken { get; set; } = string.Empty;
    [Required]
    public string Email { get; set; } = string.Empty;
    [Required]
    public string NewPassword { get; set; } = string.Empty;
    [Required]
    public string ConfirmPassword { get; set; } = string.Empty;
}