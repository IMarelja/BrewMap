using BrewMapAPI.Models;

namespace BrewMapAPI.Repository.Users;

public interface IUserRepo
{
    Task<User?> GetUserById(string id);
    Task<bool> UpdatePassword(string id, string newPasswordHash, string newPasswordSalt);
    Task<bool> UpdateEmail(string id, string newEmail);
    Task<bool> DeleteUser(string id);
}
