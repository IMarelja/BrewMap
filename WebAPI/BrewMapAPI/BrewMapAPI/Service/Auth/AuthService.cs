using BrewMapAPI.Data;
using BrewMapAPI.DTO.Auth;
using BrewMapAPI.Security;
using MongoDB.Driver;

namespace BrewMapAPI.Service.Auth
{
    public class AuthService : IAuthService
    {
        private readonly IConfiguration _config;
        private readonly MongoDbContext _context;
        public AuthService(MongoDbContext context, IConfiguration config)
        {
            _context = context;
            _config = config;
        }
        public AuthResponse Login(LoginRequest request)
        {
            try
            {
                var genericLoginFail = "Incorrect username or password";
                // MongoDB syntax for finding one user
                var existingUser = _context.Users.Find(x => x.Username == request.Username).FirstOrDefault();
                if (existingUser == null)
                {
                    return new AuthResponse()
                    {
                        Success = false,
                        Message = genericLoginFail,
                        StatusCode = 401
                    };
                }
                var hash = PasswordHashProvider.GetHash(request.Password, existingUser.PasswordSalt);
                if (hash != existingUser.PasswordHash)
                {
                    return new AuthResponse()
                    {
                        Success = false,
                        Message = genericLoginFail,
                        StatusCode = 401
                    };
                }
                var secureKey = _config["JWT:SecureKey"];
                var serializedToken = JwtTokenProvider.CreateJwtToken(secureKey, 60, request.Username, existingUser.Role);
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

        public AuthResponse Register(RegisterRequest request)
        {
            try
            {
                var trimmedUsername = request.Username.Trim();
                if (_context.Users.Find(x => x.Username == trimmedUsername).Any())
                {
                    return new AuthResponse()
                    {
                        Success = false,
                        Message = "Username is already taken",
                        StatusCode = 401
                    };
                }
                var trimmedEmail = request.Email.Trim();
                if (_context.Users.Find(x => x.Email == trimmedEmail).Any())
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
                    Role = "User",
                    ReportCount = 0
                };
                _context.Users.InsertOne(user); // MongoDB insert
                return new AuthResponse()
                {
                    Success = true,
                    Message = "User successfully registered",
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
