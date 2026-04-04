using BrewMapAPI.DTO.Auth;

namespace BrewMapAPI.Service.Auth
{
    public interface IAuthService
    {
        AuthResponse Login(LoginRequest request);
        AuthResponse Register(RegisterRequest request);
    }
}
