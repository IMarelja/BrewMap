using BrewMapAPI.DTO.User;
using BrewMapAPI.Models;

namespace BrewMapAPI.Repository.Users;

public interface IUserRepo
{
    User? GetUserById(string id);
    
}