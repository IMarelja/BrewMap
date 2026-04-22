namespace BrewMapAPI.DTO.Moderation
{
    public class ModerationResult
    {
        public bool Success { get; set; }
        public string Message { get; set; }
        public DateTime ActionPerformedAt { get; set; } = DateTime.UtcNow;
    }

    public class ModeratedUserInfo
    {
        public string Id { get; set; }
        public string Username { get; set; }
        public string Email { get; set; }
        public string Role { get; set; }
        public DateTime CreatedAt { get; set; }
    }
}