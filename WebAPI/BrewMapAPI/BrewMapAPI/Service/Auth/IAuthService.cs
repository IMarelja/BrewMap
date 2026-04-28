using BrewMapAPI.DTO.Auth;

namespace BrewMapAPI.Service.Auth
{
    public interface IAuthService
    {
        Task<AuthResponse> Login(LoginRequest request);
        Task<AuthResponse> Register(RegisterRequest request);
        Task<AuthResponse> ResetRequest(ResetRequest request);
        Task<AuthResponse> ChangePassword(ChangePasswordRequest request);
    }
}
