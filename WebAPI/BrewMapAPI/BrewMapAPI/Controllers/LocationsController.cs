using BrewMapAPI.DTO.Location;
using BrewMapAPI.Service.Location;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System.Security.Claims;

namespace BrewMapAPI.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    [Authorize] // Only logged-in users
    public class LocationsController : ControllerBase
    {
        private readonly ILocationService _service;

        public LocationsController(ILocationService service)
        {
            _service = service;
        }

        private string GetUserId()
        {
            return User.FindFirstValue(ClaimTypes.NameIdentifier);
        }

        /// <summary>
        /// Add a new cafe location
        /// </summary>
        [HttpPost]
        public async Task<ActionResult<ReadLocation>> Create([FromBody] CreateLocation dto)
        {
            var userId = GetUserId();
            var result = await _service.CreateAsync(dto, userId);
            return CreatedAtAction(nameof(GetById), new { id = result.Id }, result);
        }

        /// <summary>
        /// Get all active locations
        /// </summary>
        [HttpGet]
        [AllowAnonymous]
        public async Task<ActionResult<IEnumerable<ReadLocation>>> GetAll()
        {
            var locations = await _service.GetAllAsync();
            return Ok(locations);
        }

        /// <summary>
        /// Get a location by ID
        /// </summary>
        [HttpGet("{id}")]
        [AllowAnonymous]
        public async Task<ActionResult<ReadLocation>> GetById(string id)
        {
            var location = await _service.GetByIdAsync(id);
            if (location == null)
                return NotFound();

            return Ok(location);
        }

        /// <summary>
        /// Update a location
        /// </summary>
        [HttpPut("{id}")]
        public async Task<IActionResult> Update(string id, [FromBody] UpdateLocation dto)
        {
            if (id != dto.Id)
                return BadRequest("ID mismatch.");

            var userId = GetUserId();
            var success = await _service.UpdateAsync(dto, userId);

            if (!success)
                return NotFound();

            return NoContent();
        }

        /// <summary>
        /// Soft delete a location
        /// </summary>
        [HttpDelete("{id}")]
        public async Task<IActionResult> Delete(string id)
        {
            var userId = GetUserId();
            var success = await _service.DeleteAsync(id, userId);

            if (!success)
                return NotFound();

            return NoContent();
        }
    }
}