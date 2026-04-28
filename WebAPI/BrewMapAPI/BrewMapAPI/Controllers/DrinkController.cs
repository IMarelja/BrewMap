using System.Security.Claims;
using BrewMapAPI.DTO.Drink;
using BrewMapAPI.Service.Drinks;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace BrewMapAPI.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    [Authorize]
    public class DrinkController : ControllerBase
    {
        private readonly IDrinkService _service;

        public DrinkController(IDrinkService service)
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
                var drink = await _service.GetById(id);
                return Ok(drink);
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
                var drinks = await _service.GetByLocationId(locationId);
                return Ok(drinks);
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);
            }
        }

        [HttpGet("location/{locationId}/best-drink")]
        [Authorize(Roles =  "admin,user")]
        public async Task<IActionResult> GetBestDrinkInLocationId(string locationId)
        {
            try
            {
                var drink = await _service.GetBestDrinkByLocationId(locationId);
                if (drink == null)
                    return NotFound();
                return Ok(drink);
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);
            }
        }

        [HttpPost]
        [Authorize(Roles =  "admin,user")]
        public async Task<IActionResult> CreateDrink([FromBody] CreateDrink drink)
        {
            try
            {
                if (!ModelState.IsValid)
                    return BadRequest(ModelState);

                var created = await _service.CreateDrink(drink, GetUserId());
                return CreatedAtAction(nameof(GetById), new { id = created.Id }, created);
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);
            }
        }

        [HttpPut("{id}")]
        [Authorize(Roles =  "admin,user")]
        public async Task<IActionResult> UpdateDrink(string id, [FromBody] UpdateDrink drink)
        {
            try
            {
                if (!ModelState.IsValid)
                    return BadRequest(ModelState);


                var updated = await _service.UpdateDrink(id, drink);
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
        [Authorize(Roles =  "admin")]
        public async Task<IActionResult> DeleteDrink(string id)
        {
            try
            {
                var deleted = await _service.DeleteDrink(id);
                if (!deleted)
                    return NotFound();
                return NoContent();
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);
            }
        }
    }
}
