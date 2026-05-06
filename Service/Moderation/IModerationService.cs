using BrewMapAPI.DTO.Moderation;

namespace BrewMapAPI.Service.Moderation
{
    public interface IModerationService
    {
        // User management
        Task<ModeratedUserInfo?> GetUserInfo(string userId);
        Task<ModerationResult> UpdateUser(string userId, UpdateUserModerationRequest request);

        // Task<ModerationResult> SuspendUser(string userId);
        // Task<ModerationResult> UnsuspendUser(string userId);
        // Task<ModerationResult> GrantAdminRole(string userId);
        // Task<ModerationResult> RevokeAdminRole(string userId);
    }
}