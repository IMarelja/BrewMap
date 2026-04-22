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

        // Helper to extract userId from JWT claims
        private string? GetUserId()
        {
            return User.FindFirstValue("sub")
                ?? User.FindFirstValue("id")
                ?? User.FindFirstValue(ClaimTypes.NameIdentifier);
        }

        // Get a review by its ID (single review detail)
        [HttpGet("{id}")]
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

        // Get all reviews for a specific cafe location
        [HttpGet("cafe/{cafeId}")]
        public async Task<IActionResult> GetByCafeId(string cafeId)
        {
            try
            {
                var reviews = await _service.GetByTarget("location", cafeId);
                return Ok(reviews);
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);
            }
        }

        // Get all reviews created by the logged-in user
        [HttpGet("mine")]
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

        // Create a new review
        [HttpPost]
        public async Task<IActionResult> CreateReview([FromBody] CreateReview review)
        {
            try
            {
                var userId = GetUserId();
                if (string.IsNullOrEmpty(userId))
                    return Unauthorized();

                var createdReview = await _service.CreateReview(review, userId);
                return CreatedAtAction(nameof(GetById), new { id = createdReview.Id }, createdReview);
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);
            }
        }

        // Update own review
        [HttpPut("{id}")]
        public async Task<IActionResult> UpdateReview(string id, [FromBody] UpdateReview update)
        {
            try
            {
                var userId = GetUserId();
                if (string.IsNullOrEmpty(userId))
                    return Unauthorized();

                update.Id = id; // ensure id in route matches DTO
                var updated = await _service.UpdateReview(update, userId);
                if (updated == null)
                    return NotFound();
                return Ok(updated);
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);
            }
        }

        // Delete own review
        [HttpDelete("{id}")]
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