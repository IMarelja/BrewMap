namespace BrewMapAPI.DTO.Moderation
{
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
        public DateTime CreatedAt { get; set; }
    }
}