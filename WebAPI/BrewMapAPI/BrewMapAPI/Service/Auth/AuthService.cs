using BrewMapAPI.Data;
using BrewMapAPI.DTO.Auth;
using BrewMapAPI.Repository.Auth;
using BrewMapAPI.Security;
using Microsoft.AspNetCore.Authorization;
using Microsoft.Extensions.Caching.Memory;
using MongoDB.Driver;

namespace BrewMapAPI.Service.Auth
{
    public class AuthService : IAuthService
    {
        private readonly IConfiguration _config;
        private readonly IAuthRepo _repo;
        
        public AuthService(IConfiguration config, IAuthRepo repo)
        {
            _config = config;
            _repo = repo;
        }
        public async Task<AuthResponse> Login(LoginRequest request)
        {
            try
            {
                var genericLoginFail = "Incorrect username or password";
                // MongoDB syntax for finding one user
                var existingUser = await _repo.GetByUsername(request.Username);
                if (existingUser == null)
                {
                    return new AuthResponse()
                    {
                        Success = false,
                        Message = genericLoginFail,
                        StatusCode = 401
                    };
                }
                var hash = PasswordHashProvider.GetHash(request.Password, existingUser.PasswordSalt!);
                if (hash != existingUser.PasswordHash)
                {
                    return new AuthResponse()
                    {
                        Success = false,
                        Message = genericLoginFail,
                        StatusCode = 401
                    };
                }
                var serializedToken = JwtTokenProvider.CreateJwtToken(existingUser, _config, 60);
                return new AuthResponse()
                {
                    Success = true,
                    Message = "Login successful",
                    StatusCode = 200,
                    Token = serializedToken
                };
            }
            catch (Exception ex)
            {
                return new AuthResponse()
                {
                    Success = false,
                    Message = ex.Message,
                    StatusCode = 400
                };
            }
        }

        public async Task<AuthResponse> Register(RegisterRequest request)
        {
            try
            {
                var trimmedUsername = request.Username.Trim();
                if (await _repo.GetByUsername(trimmedUsername) != null)
                {
                    return new AuthResponse()
                    {
                        Success = false,
                        Message = "Username is already taken",
                        StatusCode = 401
                    };
                }
                var trimmedEmail = request.Email.Trim();
                if (await _repo.GetByEmail(trimmedEmail) != null)
                {
                    return new AuthResponse()
                    {
                        Success = false,
                        Message = "Email is already taken",
                        StatusCode = 401
                    };
                }
                var passSalt = PasswordHashProvider.GetSalt();
                var passHash = PasswordHashProvider.GetHash(request.Password, passSalt);
                var user = new Models.User
                {
                    Username = trimmedUsername,
                    Email = trimmedEmail,
                    PasswordHash = passHash,
                    PasswordSalt = passSalt,
                    CreatedAt = DateTime.UtcNow,
                    Role = "user",
                    ReportCount = 0
                };
                
                await _repo.Create(user);
                var serializedToken = JwtTokenProvider.CreateJwtToken(user, _config, 60);
                return new AuthResponse()
                {
                    Success = true,
                    Message = "User successfully registered",
                    StatusCode = 200,
                    Token = serializedToken
                };
            }
            catch (Exception ex)
            {
                return new AuthResponse()
                {
                    Success = false,
                    Message = ex.Message,
                    StatusCode = 400,
                };
            }
        }
    }
}
