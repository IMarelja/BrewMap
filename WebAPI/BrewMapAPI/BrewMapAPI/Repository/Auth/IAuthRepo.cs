using BrewMapAPI.DTO.Auth;
using BrewMapAPI.Models;

namespace BrewMapAPI.Repository.Auth;

public interface IAuthRepo
{
    Task<User?> GetByUsername(string username);
    Task<User?> GetByEmail(string email);
    Task<User?> GetById(string id);
    Task<User> Create(User user);

}