using BrewMapAPI.Service.Moderation;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace BrewMapAPI.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    [Authorize(Roles = "admin")]
    public class ModerationController : ControllerBase
    {
        private readonly IModerationService _service;

        public ModerationController(IModerationService service)
        {
            _service = service;
        }


        /// GET: api/Moderation/user/{id}
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

        /// PATCH: api/Moderation/user/{id}
        [HttpPatch("user/{id}")]
        public async Task<IActionResult> UpdateUser(string id, [FromBody] UpdateUserModerationRequest request)
        {
            try
            {
                var result = await _service.UpdateUser(id, request);

                if (!result.Success)
                    return BadRequest(result);

                return Ok(result);
            }
            catch (Exception ex)
            {
                return BadRequest(new { success = false, message = ex.Message });
            }
        }

        /// PUT: api/Moderation/User/{id}/unsuspend
        [HttpPut("User/{id}/unsuspend")]
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

        

        // /// PUT: api/Moderation/User/{id}/suspend
        // [HttpPut("User/{id}/suspend")]
        // public async Task<IActionResult> SuspendUser(string id)
        // {
        //     try
        //     {
        //         var result = await _service.SuspendUser(id);
        //         if (!result.Success)
        //             return BadRequest(result);
        //         return Ok(result);
        //     }
        //     catch (Exception ex)
        //     {
        //         return BadRequest(new { success = false, message = ex.Message });
        //     }
        // }

        // /// PUT: api/Moderation/User/{id}/unsuspend
        // [HttpPut("User/{id}/unsuspend")]
        // public async Task<IActionResult> UnsuspendUser(string id)
        // {
        //     try
        //     {
        //         var result = await _service.UnsuspendUser(id);
        //         if (!result.Success)
        //             return BadRequest(result);
        //         return Ok(result);
        //     }
        //     catch (Exception ex)
        //     {
        //         return BadRequest(new { success = false, message = ex.Message });
        //     }
        // }

        // /// PUT: api/Moderation/User/{id}/grant-admin
        // [HttpPut("User/{id}/grant-admin")]
        // public async Task<IActionResult> GrantAdminRole(string id)
        // {
        //     try
        //     {
        //         var result = await _service.GrantAdminRole(id);
        //         if (!result.Success)
        //             return BadRequest(result);
        //         return Ok(result);
        //     }
        //     catch (Exception ex)
        //     {
        //         return BadRequest(new { success = false, message = ex.Message });
        //     }
        // }

        // /// PUT: api/Moderation/User/{id}/revoke-admin
        // [HttpPut("user/{id}/revoke-admin")]
        // public async Task<IActionResult> RevokeAdminRole(string id)
        // {
        //     try
        //     {
        //         var result = await _service.RevokeAdminRole(id);
        //         if (!result.Success)
        //             return BadRequest(result);
        //         return Ok(result);
        //     }
        //     catch (Exception ex)
        //     {
        //         return BadRequest(new { success = false, message = ex.Message });
        //     }
        // }
    }
}