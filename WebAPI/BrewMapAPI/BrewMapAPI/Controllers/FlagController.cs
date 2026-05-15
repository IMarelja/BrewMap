using BrewMapAPI.DTO.Flag;
using BrewMapAPI.Service.Flags;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System.ComponentModel.DataAnnotations;
using System.Diagnostics.CodeAnalysis;
using System.Security.Claims;

namespace BrewMapAPI.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class FlagController : ControllerBase
    {
        private readonly IFlagService _service;

        public FlagController(IFlagService service)
        {
            _service = service;
        }

        private string GetUserId() => User.FindFirstValue(ClaimTypes.NameIdentifier)!;

        // POST: api/flag - Create a report (User)
        [HttpPost]
        [Authorize(Roles = "admin,user")]
        public async Task<IActionResult> CreateFlag([FromBody] CreateFlag flag)
        {
            try
            {
                if (!ModelState.IsValid)
                    return BadRequest(ModelState);

                var userId = GetUserId();
                var created = await _service.CreateFlag(flag, userId);
                return CreatedAtAction(nameof(GetById), new { id = created.Id }, created);
            }
            catch (InvalidOperationException ex)
            {
                return BadRequest(ex.Message);
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);
            }
        }

        // GET: api/flag/{id} - Get a specific report (Admin)
        [HttpGet("{id}")]
        [Authorize(Roles =  "admin")]
        public async Task<IActionResult> GetById(string id)
        {
            try
            {
                var flag = await _service.GetById(id);
                if (flag == null)
                    return NotFound();
                return Ok(flag);
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);
            }
        }

        // GET: api/flag - Get all reports with optional filters (Admin)
        // Query params: ?status=pending&targetType=location
        [HttpGet]
        [Authorize(Roles =  "admin")]
        public async Task<IActionResult> GetAll(
            [FromQuery] 
            [AllowNull]
            [AllowedValues("pending", "reviewed", "resolved")]
            string? status, 
            
            [FromQuery] 
            [AllowNull]
            [AllowedValues("location", "product", "review", "user")] 
            string? targetType
        )
        {
            try
            {
                var flags = await _service.GetAll(status, targetType);
                return Ok(flags);
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);
            }
        }

        // PUT: api/flag/status - Update report status (Admin)
        [HttpPut("status")]
        [Authorize(Roles =  "admin")]
        public async Task<IActionResult> UpdateStatus([FromBody] UpdateFlagStatus dto)
        {
            try
            {
                if (!ModelState.IsValid)
                    return BadRequest(ModelState);

                var updated = await _service.UpdateStatus(dto);
                if (updated == null)
                    return NotFound();
                return Ok(updated);
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);
            }
        }

        // GET: api/flag/stats - Get report statistics (Admin)
        [HttpGet("stats")]
        [Authorize(Roles =  "admin")]
        public async Task<IActionResult> GetStatistics()
        {
            try
            {
                var stats = await _service.GetStatistics();
                return Ok(stats);
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);
            }
        }
    }
}