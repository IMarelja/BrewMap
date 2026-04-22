using BrewMapAPI.DTO.Moderation;

namespace BrewMapAPI.Service.Moderation
{
    public interface IModerationService
    {
        // Content management
        Task<ModerationResult> DeleteLocation(string id);
        Task<ModerationResult> DeleteDrink(string id);
        Task<ModerationResult> DeleteReview(string id);

        // User management
        Task<ModeratedUserInfo?> GetUserInfo(string userId);
        Task<ModerationResult> SuspendUser(string userId);
        Task<ModerationResult> UnsuspendUser(string userId);
        Task<ModerationResult> GrantAdminRole(string userId);
        Task<ModerationResult> RevokeAdminRole(string userId);
    }
}