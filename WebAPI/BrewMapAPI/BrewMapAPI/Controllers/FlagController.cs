using BrewMapAPI.DTO.Flag;
using BrewMapAPI.Models;
using BrewMapAPI.Service.Flags;
using Microsoft.AspNetCore.Mvc;

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

        [HttpPost]
        public async Task<IActionResult> CreateFlag([FromBody] CreateFlag flag)
        {
            try
            {
                if (!ModelState.IsValid)
                    return BadRequest(ModelState);

                var created = await _service.CreateFlag(flag);
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

        [HttpGet("{id}")]
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

        [HttpGet]
        public async Task<IActionResult> GetAll([FromQuery] FlagStatus? status, [FromQuery] ContentType? contentType)
        {
            try
            {
                var flags = await _service.GetAll(status, contentType);
                return Ok(flags);
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);
            }
        }

        [HttpPut("status")]
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

        [HttpGet("stats")]
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