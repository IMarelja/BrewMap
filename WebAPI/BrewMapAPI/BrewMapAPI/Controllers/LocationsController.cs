using BrewMapAPI.DTO.Location;
using BrewMapAPI.Service.Location;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System.ComponentModel.DataAnnotations;
using System.Security.Claims;

namespace BrewMapAPI.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    [Authorize]
    public class LocationsController : ControllerBase
    {
        private readonly ILocationService _service;

        public LocationsController(ILocationService service)
        {
            _service = service;
        }

        [Authorize(Roles =  "admin,user")]
        private string GetUserId()
        {
            var userId = User.FindFirstValue(ClaimTypes.NameIdentifier);
            if (string.IsNullOrWhiteSpace(userId))
                throw new UnauthorizedAccessException("Invalid authentication token.");

            return userId;
        }

        /// <summary>
        /// Add a new cafe location
        /// </summary>
        [HttpPost]
        [Authorize(Roles =  "admin,user")]
        public async Task<ActionResult<ReadLocation>> Create([FromBody] CreateLocation dto)
        {

            try
            {
                var userId = GetUserId();
                var result = await _service.CreateAsync(dto, userId);
                return CreatedAtAction(nameof(GetById), new { id = result.Id }, result);
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);
            }
        }

        /// <summary>
        /// Get all active locations
        /// </summary>
        [HttpGet]
        [Authorize(Roles =  "admin,user")]
        public async Task<ActionResult<IEnumerable<ReadLocation>>> GetAll()
        {
            try
            {
                var locations = await _service.GetAllAsync();
                return Ok(locations);
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);
            }
        }

        /// <summary>
        /// Get a location by ID
        /// </summary>
        [HttpGet("{id}")]
        [Authorize(Roles =  "admin,user")]
        public async Task<ActionResult<ReadLocation>> GetById(string id)
        {
            try
            {
                var location = await _service.GetByIdAsync(id);
                if (location == null)
                    return NotFound(new { message = "Location not found." });

                return Ok(location);
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);
            }
        }

        /// <summary>
        /// Search locations by query, filters, and optional geolocation
        /// </summary>
        [HttpGet("search")]
        [Authorize(Roles = "admin,user")]
        public async Task<ActionResult<IEnumerable<ReadLocation>>> Search(
            [FromQuery] string? query,
            [FromQuery] double? minRating,
            [FromQuery] string? drinkQuery,
            [FromQuery] List<string>? paymentOptionTags,
            [FromQuery] [Required] double latitude,
            [FromQuery] [Required] double longitude,
            [FromQuery] double radiusMeters = 5000)
        {
            try
            {
                var result = await _service.SearchAsync(
                    query, minRating, drinkQuery, paymentOptionTags, latitude, longitude, radiusMeters);

                return Ok(result);
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);
            }
        }

        /// <summary>
        /// Update a location
        /// </summary>
        [HttpPut("{id}")]
        [Authorize(Roles =  "admin,user")]
        public async Task<IActionResult> Update(string id, [FromBody] UpdateLocation dto)
        {
            try
            {
                var userId = GetUserId();
                var success = await _service.UpdateAsync(id, dto, userId);

                if (!success)
                    return NotFound(new { message = "Location not found or access denied." });

                return NoContent();
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);
            }

        }

        /// <summary>
        /// Soft delete a location
        /// </summary>
        [HttpDelete("{id}")]
        [Authorize(Roles = "admin")]
        public async Task<IActionResult> Delete(string id)
        {
            try
            {
                var userId = GetUserId();
                var success = await _service.DeleteAsync(id, userId);

                if (!success)
                    return NotFound(new { message = "Location not found or access denied." });

                return NoContent();
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);
            }
        }
    }
}
