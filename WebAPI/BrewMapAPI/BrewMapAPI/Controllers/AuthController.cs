using System.Security.Claims;
using System.Text;
using BrewMapAPI.DTO.Auth;
using BrewMapAPI.Models;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using System.IdentityModel.Tokens.Jwt;
using BrewMapAPI.Security;
using Microsoft.AspNetCore.Authorization;
using Microsoft.IdentityModel.Tokens;

namespace BrewMapAPI.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class AuthController : ControllerBase
    {
        DbContext _context;
        IConfiguration _config;
        
        public AuthController(IConfiguration config)
        {
            _config = config;
        }

        [HttpPost("login")]
        [AllowAnonymous]
        public IActionResult Login([FromBody] LoginRequest loginUser)
        {
            try
            {
                var genericLoginFail = "Incorrect username or password";
                
                var existingUser = _context.Users.FirstOrDefault(x => x.Username == loginUser.Username);
                if (existingUser == null)
                {
                    return Unauthorized(genericLoginFail);
                }
                
                var hash = PasswordHashProvider.GetHash(loginUser.Password, existingUser.PassSalt);
                if (hash != existingUser.PassHash)
                {
                    return Unauthorized(genericLoginFail);
                }
                
                var secureKey = _config["JWT:SecureKey"];
                var serializedToken = JwtTokenProvider.CreateJwtToken(secureKey, 60, loginUser.Username, "User");
                return Ok(serializedToken);
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);
            }
            
        }

        [HttpPost("register")]
        [AllowAnonymous]
        public IActionResult Register([FromBody] RegisterRequest registerUser)
        {
            try
            {
                var trimmedUsername = registerUser.Username.Trim();
                if (_context.Users.Any(x => x.Username.Equals(trimmedUsername)))
                {
                    return BadRequest();
                }
                var trimmedEmail = registerUser.Email.Trim();
                if (_context.Users.Any(x => x.Email.Equals(trimmedEmail)))
                {
                    return BadRequest();
                }

                var passSalt = PasswordHashProvider.GetSalt();
                var passHash = PasswordHashProvider.GetHash(registerUser.Password, passSalt);

                var user = new User
                {
                    Id = null,
                    Username = trimmedUsername,
                    Email = trimmedEmail,
                    PasswordHash = passHash,
                    PasswordSalt = passSalt,
                    CreatedAt = DateTime.UtcNow,
                    Role = "User"
                };
            
                _context.Add(user);
                _context.SaveChanges();
                return Ok(registerUser);
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);
            }
        }
        
    }
}
