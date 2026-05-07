using BrewMapAPI.Service.Moderation;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace BrewMapAPI.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class ModerationController : ControllerBase
    {
        private readonly IModerationService _service;

        public ModerationController(IModerationService service)
        {
            _service = service;
        }

        /// DELETE: api/moderation/location/{id}
        [HttpDelete("location/{id}")]
        [Authorize(Roles =  "admin")]
        public async Task<IActionResult> DeleteLocation(string id)
        {
            try
            {
                var result = await _service.DeleteLocation(id);
                
                if (!result.Success)
                    return NotFound(result);
                
                return Ok(result);
            }
            catch (Exception ex)
            {
                return BadRequest(new { success = false, message = ex.Message });
            }
        }

        /// DELETE: api/moderation/drink/{id}
        [HttpDelete("drink/{id}")]
        [Authorize(Roles =  "admin")]
        public async Task<IActionResult> DeleteDrink(string id)
        {
            try
            {
                var result = await _service.DeleteDrink(id);
                
                if (!result.Success)
                    return NotFound(result);
                
                return Ok(result);
            }
            catch (Exception ex)
            {
                return BadRequest(new { success = false, message = ex.Message });
            }
        }

        /// DELETE: api/moderation/review/{id}
        [HttpDelete("review/{id}")]
        [Authorize(Roles =  "admin")]
        public async Task<IActionResult> DeleteReview(string id)
        {
            try
            {
                var result = await _service.DeleteReview(id);
                
                if (!result.Success)
                    return NotFound(result);
                
                return Ok(result);
            }
            catch (Exception ex)
            {
                return BadRequest(new { success = false, message = ex.Message });
            }
        }

        /// GET: api/moderation/user/{id}
        [HttpGet("user/{id}")]
        public async Task<IActionResult> GetUserInfo(string id)
        {
            try
            {
                var user = await _service.GetUserInfo(id);
                
                if (user == null)
                    return NotFound(new { success = false, message = "User not found." });
                
                return Ok(user);
            }
            catch (Exception ex)
            {
                return BadRequest(new { success = false, message = ex.Message });
            }
        }

        /// PUT: api/moderation/user/{id}/suspend
        [HttpPut("user/{id}/suspend")]
        [Authorize(Roles =  "admin")]
        public async Task<IActionResult> SuspendUser(string id)
        {
            try
            {
                var result = await _service.SuspendUser(id);
                
                if (!result.Success)
                    return BadRequest(result);
                
                return Ok(result);
            }
            catch (Exception ex)
            {
                return BadRequest(new { success = false, message = ex.Message });
            }
        }

        /// PUT: api/moderation/user/{id}/unsuspend
        [HttpPut("user/{id}/unsuspend")]
        [Authorize(Roles =  "admin")]
        public async Task<IActionResult> UnsuspendUser(string id)
        {
            try
            {
                var result = await _service.UnsuspendUser(id);
                
                if (!result.Success)
                    return BadRequest(result);
                
                return Ok(result);
            }
            catch (Exception ex)
            {
                return BadRequest(new { success = false, message = ex.Message });
            }
        }

        /// PUT: api/moderation/user/{id}/grant-admin
        [HttpPut("user/{id}/grant-admin")]
        [Authorize(Roles =  "admin")]
        public async Task<IActionResult> GrantAdminRole(string id)
        {
            try
            {
                var result = await _service.GrantAdminRole(id);
                
                if (!result.Success)
                    return BadRequest(result);
                
                return Ok(result);
            }
            catch (Exception ex)
            {
                return BadRequest(new { success = false, message = ex.Message });
            }
        }

        /// PUT: api/moderation/user/{id}/revoke-admin
        [HttpPut("user/{id}/revoke-admin")]
        [Authorize(Roles =  "admin")]
        public async Task<IActionResult> RevokeAdminRole(string id)
        {
            try
            {
                var result = await _service.RevokeAdminRole(id);
                
                if (!result.Success)
                    return BadRequest(result);
                
                return Ok(result);
            }
            catch (Exception ex)
            {
                return BadRequest(new { success = false, message = ex.Message });
            }
        }
    }
}