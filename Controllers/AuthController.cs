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
using BrewMapAPI.Service.Auth;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Identity.Data;
using Microsoft.IdentityModel.Tokens;
using LoginRequest = BrewMapAPI.DTO.Auth.LoginRequest;
using RegisterRequest = BrewMapAPI.DTO.Auth.RegisterRequest;

namespace BrewMapAPI.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class AuthController : ControllerBase
    {
        private readonly IAuthService _authService;

        public AuthController(IAuthService authService)
        {
            _authService = authService;
        }

        [HttpPatch("login")]
        [AllowAnonymous]
        public async Task<IActionResult> Login([FromBody] LoginRequest loginUser)
        {
            if (!ModelState.IsValid)
            {
                return BadRequest(ModelState);
            }
            try
            {
                AuthResponse response = await _authService.Login(loginUser);
                return StatusCode(response.StatusCode, response);
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);
            }
        }

        [HttpPost("register")]
        [AllowAnonymous]
        public async Task<IActionResult> Register([FromBody] RegisterRequest registerUser)
        {
            if (!ModelState.IsValid)
            {
                return BadRequest(ModelState);
            }
            try
            {
                AuthResponse response = await _authService.Register(registerUser);
                return StatusCode(response.StatusCode, response);
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);
            }
        }
        
        [HttpPost("send-reset-email")]
        [AllowAnonymous]
        public async Task<IActionResult> SendResetEmail([FromBody] ResetRequest resetRequest)
        {
            if (!ModelState.IsValid)
            {
                return BadRequest(ModelState);
            }
            try
            {
                AuthResponse response = await _authService.ResetRequest(resetRequest);
                return StatusCode(response.StatusCode, response.Message);
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);
            }
        }
        
        [HttpPatch("change-password")]
        [AllowAnonymous]
        public async Task<IActionResult> ChangePassword([FromBody] ChangePasswordRequest passwordRequest)
        {
            if (!ModelState.IsValid)
            {
                return BadRequest(ModelState);
            }
            try
            {
                AuthResponse response = await _authService.ChangePassword(passwordRequest);
                return StatusCode(response.StatusCode, response.Message);
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);
            }
        }
    }
}