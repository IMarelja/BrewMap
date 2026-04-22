using BrewMapAPI.Models;

namespace BrewMapAPI.Repository.Moderation
{
    public interface IModerationRepo
    {
        //Content management
        Task<bool> DeleteLocation(string id);
        Task<bool> DeleteDrink(string id);
        Task<bool> DeleteReview(string id);

        //User management
        Task<User?> GetUserById(string id);
        Task<User?> UpdateUserRole(string userId, string newRole);
    }
}