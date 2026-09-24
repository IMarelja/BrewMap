using BrewMapAPI.Models;

namespace BrewMapAPI.Repository.Moderation
{
    public interface IModerationRepo
    {
        //User management
        Task<User?> GetUserById(string id);
        Task<List<User>> GetUsersByKeyword(string keyword);
        Task<User?> UpdateUserRole(string userId, string newRole);
        Task<User?> UpdateUserIsActive(string userId, bool isActive);
    }
}