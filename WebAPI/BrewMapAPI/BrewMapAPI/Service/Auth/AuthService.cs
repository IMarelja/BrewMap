using System.Net.Mail;
using BrewMapAPI.Data;
using BrewMapAPI.DTO.Auth;
using BrewMapAPI.Models;
using BrewMapAPI.Repository.Auth;
using BrewMapAPI.Security;
using Microsoft.AspNetCore.Authorization;
using Microsoft.Extensions.Caching.Memory;
using MimeKit;
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
                var genericLoginFail = "Incorrect email, username or password";
                // MongoDB syntax for finding one user
                var emailUser = await _repo.GetByEmail(request.User);
                var usernameUser = await _repo.GetByUsername(request.User);
                Models.User existingUser = null;
                if (emailUser != null)
                {
                    existingUser = emailUser;
                }
                if (usernameUser != null)
                {
                    existingUser = usernameUser;
                }
                if (existingUser == null)
                {
                    return new AuthResponse()
                    {
                        Success = false,
                        Message = genericLoginFail,
                        StatusCode = 401
                    };
                }

                if (existingUser.IsActive)
                {
                    return new AuthResponse()
                    {
                        Success = false,
                        Message = "Already logged in",
                        StatusCode = 400
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
                existingUser.LastLoginAt = DateTime.UtcNow;
                await _repo.Update(existingUser);
                int expiration = 60;
                if (request.RememberMe)
                {
                    //token lasts for 30 days
                    expiration = 1440;
                }
                var serializedToken = JwtTokenProvider.CreateJwtToken(existingUser, _config, expiration);
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
                    LastLoginAt = DateTime.UtcNow,
                    Role = "user",
                    ReportCount = 0
                };
                var serializedToken = JwtTokenProvider.CreateJwtToken(user, _config, 60);
                await _repo.Create(user);
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

        public async Task<AuthResponse> ResetRequest(ResetRequest request)
        {
            try
            {
                var existingUser = await _repo.GetByEmail(request.Email);
                if (existingUser == null)
                {
                    return new AuthResponse()
                    {
                        Success = false,
                        Message = "User not found",
                        StatusCode = 404
                    };
                }
                var refreshToken = PasswordTokenProvider.GenerateSecureToken();
                var token = new TempToken()
                {
                    UserId = existingUser.Id,
                    Token = refreshToken,
                    ExpiresAt = DateTime.UtcNow.AddDays(3),
                };
                await _repo.CreateToken(token);
                //string body = $"Dear {existingUser.Username}, please use this token to reset your password: {refreshToken}";
                //WHY
                //await new SmtpClient("smtp.gmail.com", 465).SendMailAsync("BrewMap@gmail.com", existingUser.Email, "Password Reset", body);
                
                //var message = new MimeMessage ();
                //message.From.Add (new MailboxAddress ("BrewMap Admin", "zara.capps@yahoo.co.uk"));
                //message.To.Add (new MailboxAddress (existingUser.Username, existingUser.Email));
                //message.Subject = "Password Reset";

                //message.Body = new TextPart ("plain") {
                    //Text = $"Dear {existingUser.Username}, please use this token to reset your password: {body}. --BrewMap Admin"
                //};
                
                return new AuthResponse()
                {
                    Success = true,
                    Message = "Reset email sent",
                    StatusCode = 200,
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

        public async Task<AuthResponse> ChangePassword(ChangePasswordRequest request)
        {
            try
            {
                var invalidToken = "Invalid Token";
                if (request.NewPassword != request.ConfirmPassword)
                {
                    return new AuthResponse()
                    {
                        Success = false,
                        Message = "Passwords do not match",
                        StatusCode = 401
                    };
                }
                var existingUser = await _repo.GetByEmail(request.Email);
                if (existingUser == null)
                {
                    return new AuthResponse()
                    {
                        Success = false,
                        Message = "User not found",
                        StatusCode = 404
                    };
                }
                var token = await _repo.GetToken(request.ResetToken);
                if (token == null)
                {
                    return new AuthResponse()
                    {
                        Success = false,
                        Message = invalidToken,
                        StatusCode = 401
                    };
                }
                if (token.ExpiresAt < DateTime.UtcNow)
                {
                    return new AuthResponse()
                    {
                        Success = false,
                        Message = invalidToken,
                        StatusCode = 401
                    };
                }
                var tokenUser = await _repo.GetById(token.UserId);
                if (tokenUser == null)
                {
                    return new AuthResponse()
                    {
                        Success = false,
                        Message = invalidToken,
                        StatusCode = 401
                    };
                }
                if (tokenUser != existingUser)
                {
                    return new AuthResponse()
                    {
                        Success = false,
                        Message = invalidToken,
                        StatusCode = 401
                    };
                }
                var passSalt = PasswordHashProvider.GetSalt();
                var passHash = PasswordHashProvider.GetHash(request.NewPassword, passSalt);
                existingUser.PasswordSalt = passSalt;
                existingUser.PasswordHash = passHash;
                await _repo.Update(existingUser);
                return new AuthResponse()
                {
                    Success = true,
                    Message = "User successfully updated",
                    StatusCode = 200
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
