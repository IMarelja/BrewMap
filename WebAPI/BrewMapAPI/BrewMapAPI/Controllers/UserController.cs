using System.Security.Claims;
using BrewMapAPI.DTO.User;
using BrewMapAPI.Service.User;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace BrewMapAPI.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    [Authorize]
    public class UserController : ControllerBase
    {
        private readonly IUserService _service;

        public UserController(IUserService userService)
        {
            _service = userService;
        }

        private string GetUserId() => User.FindFirstValue(ClaimTypes.NameIdentifier)!;

        [HttpGet("me")]
        [Authorize(Roles =  "admin,user")]
        public async Task<IActionResult> GetMyProfile()
        {
            try
            {
                MyUserProfileRead? profile = await _service.GetMyProfile(GetUserId());

                if (profile == null)
                    return NotFound();

                return Ok(profile);
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);
            }
        }

        [HttpGet("{id}")]
        [Authorize(Roles =  "admin,user")]
        public async Task<IActionResult> GetUserById(string id)
        {
            try
            {
                StrangerUserProfileRead? profile = await _service.GetUserById(id);

                if (profile == null)
                    return NotFound();

                return Ok(profile);
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);
            }
        }

        [HttpPut("email")]
        [Authorize(Roles =  "admin,user")]
        public async Task<IActionResult> UpdateEmail([FromBody] UpdateEmail dto)
        {
            if (!ModelState.IsValid) 
                return BadRequest(ModelState);

            try
            {
                var result = await _service.UpdateEmail(GetUserId(), dto);
                return StatusCode(result.StatusCode, result);
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);
            }
        }

        [HttpPut("password")]
        [Authorize(Roles =  "admin,user")]
        public async Task<IActionResult> UpdatePassword([FromBody] UpdatePassword dto)
        {
            if (!ModelState.IsValid) 
                return BadRequest(ModelState);

            try
            {
                var result = await _service.UpdatePassword(GetUserId(), dto);
                return StatusCode(result.StatusCode, result);
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);
            }
        }

        [HttpGet("me/export")]
        public async Task<IActionResult> ExportMyData()
        {
            try
            {
                var export = await _service.ExportMyData(GetUserId());

                if (export == null)
                    return NotFound();

                return Ok(export);
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);
            }
        }

        [HttpDelete("me")]
        [Authorize(Roles =  "admin,user")]
        public async Task<IActionResult> DeleteMyAccount()
        {
            try
            {
                var result = await _service.DeleteMyAccount(GetUserId());
                return StatusCode(result.StatusCode, result);
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);
            }
        }
    }
}
