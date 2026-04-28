using BrewMapAPI.DTO.Pin;
using BrewMapAPI.Service.Pins;
using Microsoft.AspNetCore.Mvc;

namespace BrewMapAPI.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class PinController : ControllerBase
    {
        private readonly IPinService _service;

        public PinController(IPinService service)
        {
            _service = service;
        }

        [HttpGet("{id}")]
        public async Task<IActionResult> GetById(string id)
        {
            try
            {
                var pin = await _service.GetById(id);
                return Ok(pin);
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);
            }
        }

        [HttpGet("range")]
        public async Task<IActionResult> GetByRange(
            [FromQuery] double minLat,
            [FromQuery] double maxLat,
            [FromQuery] double minLon,
            [FromQuery] double maxLon)
        {
            try
            {
                var pins = await _service.GetByRange(minLat, maxLat, minLon, maxLon);
                return Ok(pins);
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);
            }
        }
    }
}