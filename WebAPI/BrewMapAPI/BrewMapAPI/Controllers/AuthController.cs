using System.Security.Claims;
using System.Text;
using BrewMapAPI.DTO.Auth;
using BrewMapAPI.Models;
using BrewMapAPI.Data;
using MongoDB.Driver;
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
        private readonly MongoDbContext _context;
        private readonly IConfiguration _config;

        public AuthController(MongoDbContext context, IConfiguration config)
        {
            _context = context;
            _config = config;
        }

        [HttpPost("login")]
        [AllowAnonymous]
        public IActionResult Login([FromBody] LoginRequest loginUser)
        {
            try
            {
                var genericLoginFail = "Incorrect username or password";
                // MongoDB syntax for finding one user
                var existingUser = _context.Users.Find(x => x.Username == loginUser.Username).FirstOrDefault();
                if (existingUser == null)
                {
                    return Unauthorized(genericLoginFail);
                }
                var hash = PasswordHashProvider.GetHash(loginUser.Password, existingUser.PasswordSalt);
                if (hash != existingUser.PasswordHash)
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
                if (_context.Users.Find(x => x.Username == trimmedUsername).Any())
                {
                    return BadRequest();
                }
                var trimmedEmail = registerUser.Email.Trim();
                if (_context.Users.Find(x => x.Email == trimmedEmail).Any())
                {
                    return BadRequest();
                }
                var passSalt = PasswordHashProvider.GetSalt();
                var passHash = PasswordHashProvider.GetHash(registerUser.Password, passSalt);
                var user = new User
                {
                    Username = trimmedUsername,
                    Email = trimmedEmail,
                    PasswordHash = passHash,
                    PasswordSalt = passSalt,
                    CreatedAt = DateTime.UtcNow,
                    Role = "User"
                };
                _context.Users.InsertOne(user); // MongoDB insert
                return Ok(registerUser);
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);
            }
        }
    }
}