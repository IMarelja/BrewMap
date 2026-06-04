using BrewMapAPI.DTO.Review;
using BrewMapAPI.Service.Review;
using Microsoft.AspNetCore.Mvc;
using System.Security.Claims;
using Microsoft.AspNetCore.Authorization;

namespace BrewMapAPI.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    //[Authorize]
    public class ReviewController : ControllerBase
    {
        private readonly IReviewService _service;

        public ReviewController(IReviewService service)
        {
            _service = service;
        }

        private string GetUserId() => User.FindFirstValue(ClaimTypes.NameIdentifier)!;

        [HttpGet("{id}")]
        [Authorize(Roles =  "admin,user")]
        public async Task<IActionResult> GetById(string id)
        {
            try
            {
                var review = await _service.GetById(id);
                if (review == null)
                    return NotFound();
                return Ok(review);
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);
            }
        }

        [HttpGet("location/{locationId}")]
        [Authorize(Roles =  "admin,user")]
        public async Task<IActionResult> GetByLocationId(string locationId)
        {
            try
            {
                var reviews = await _service.GetByTarget("location", locationId);
                return Ok(reviews);
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);
            }
        }

        [HttpGet("drink/{drinkId}")]
        [Authorize(Roles =  "admin,user")]
        public async Task<IActionResult> GetByDrinkId(string drinkId)
        {
            try
            {
                var reviews = await _service.GetByTarget("product", drinkId);
                return Ok(reviews);
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);
            }
        }

        [HttpGet("mine")]
        [Authorize(Roles =  "admin,user")]
        public async Task<IActionResult> GetOwnReviews()
        {
            try
            {
                var userId = GetUserId();
                if (string.IsNullOrEmpty(userId))
                    return Unauthorized();

                var reviews = await _service.GetByUserId(userId);
                return Ok(reviews);
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);
            }
        }

        [HttpGet("byUser/{userId}")]
        [Authorize(Roles =  "admin,user")]
        public async Task<IActionResult> GetByUserIdReviews(string userId)
        {
            try
            {
                var reviews = await _service.GetByUserId(userId);
                return Ok(reviews);
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);
            }
        }

        [HttpPost("location/{locationId}")]
        [Authorize(Roles =  "admin,user")]
        public async Task<IActionResult> CreateLocationReview(string locationId, [FromBody] CreateReviewBody body)
        {
            try
            {
                var userId = GetUserId();
                if (string.IsNullOrEmpty(userId))
                    return Unauthorized();

                var created = await _service.CreateLocationReview(locationId, body, userId);
                return CreatedAtAction(nameof(GetById), new { id = created.Id }, created);
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);
            }
        }

        [HttpPost("drink/{drinkId}")]
        public async Task<IActionResult> CreateDrinkReview(string drinkId, [FromBody] CreateReviewBody body)
        {
            try
            {
                var userId = GetUserId();
                if (string.IsNullOrEmpty(userId))
                    return Unauthorized();

                var created = await _service.CreateDrinkReview(drinkId, body, userId);
                return CreatedAtAction(nameof(GetById), new { id = created.Id }, created);
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);
            }
        }

        [HttpPut("{id}")]
        [Authorize(Roles =  "admin,user")]
        public async Task<IActionResult> UpdateReview(string id, [FromBody] UpdateReview update)
        {
            try
            {
                var userId = GetUserId();
                if (string.IsNullOrEmpty(userId))
                    return Unauthorized();

                var updated = await _service.UpdateReview(id, update, userId);
                if (updated == null)
                    return NotFound();
                return Ok(updated);
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);
            }
        }

        [HttpDelete("{id}")]
        [Authorize(Roles =  "admin,user")]
        public async Task<IActionResult> DeleteReview(string id)
        {
            try
            {
                var userId = GetUserId();
                if (string.IsNullOrEmpty(userId))
                    return Unauthorized();

                var deleted = await _service.DeleteReview(id, userId);
                if (deleted)
                    return NoContent();
                else
                    return NotFound();
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);
            }
        }
    }
}
