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
            throw new NotImplementedException();
        }

        [HttpPost]
        [Authorize(Roles =  "admin,user")]
        public async Task<IActionResult> CreateDrink([FromBody] CreateDrink drink)
        {
            throw new NotImplementedException();
        }

        [HttpPut]
        [Authorize(Roles =  "admin,user")]
        public async Task<IActionResult> UpdateDrink([FromBody] UpdateDrink drink)
        {
            throw new NotImplementedException();
        }

        [HttpDelete("{id}")]
        [Authorize(Roles =  "admin")]
        public async Task<IActionResult> DeleteDrink(string id)
        {
            throw new NotImplementedException();
        }
    }
}
