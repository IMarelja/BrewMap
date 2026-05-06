using BrewMapAPI.DTO.Auth;
using BrewMapAPI.Models;

namespace BrewMapAPI.Repository.Auth;

public interface IAuthRepo
{
    Task<User?> GetByUsername(string username);
    Task<User?> GetByEmail(string email);
    Task<User?> GetById(string id);
    Task<User> Create(User user);
    Task<User> Update(User user);
    Task<TempToken?> GetToken(string token);
    Task<TempToken> CreateToken(TempToken token);

}