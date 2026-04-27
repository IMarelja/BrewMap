using System.ComponentModel.DataAnnotations;
using System.Diagnostics.CodeAnalysis;

namespace BrewMapAPI.DTO.Moderation
{
    public class UpdateUserModerationRequest
    {
        [AllowNull]
        public bool? Suspended { get; set; }    // true = suspend, false = unsuspend

        [AllowNull]
        [AllowedValues("admin", "user", null)]
        public string? Role { get; set; }        // "admin" | "user"
    }

    public class ModerationResult
    {
        public bool Success { get; set; }
        public string Message { get; set; } = String.Empty;
        public DateTime ActionPerformedAt { get; set; } = DateTime.UtcNow;
    }

    public class ModeratedUserInfo
    {
        public string Id { get; set; } = String.Empty;
        public string Username { get; set; } = String.Empty;
        public string Email { get; set; } = String.Empty;
        public string Role { get; set; } = String.Empty;
        public bool IsActive { get; set; }
        public DateTime CreatedAt { get; set; }
    }
}