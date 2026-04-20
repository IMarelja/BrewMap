using BrewMapAPI.DTO.Auth;

namespace BrewMapAPI.Service.Auth
{
    public interface IAuthService
    {
        Task<AuthResponse> Login(LoginRequest request);
        Task<AuthResponse> Register(RegisterRequest request);
    }
}
