using System.ComponentModel.DataAnnotations;

namespace BrewMapAPI.DTO.Auth;

public class ResetRequest
{
    [Required]
    public string Email { get; set; } = string.Empty;
}