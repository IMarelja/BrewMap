using BrewMapAPI.Models;

namespace BrewMapAPI.Repository.Moderation
{
    public interface IModerationRepo
    {
        //User management
        Task<User?> GetUserById(string id);
        Task<User?> UpdateUserRole(string userId, string newRole);
    }
}