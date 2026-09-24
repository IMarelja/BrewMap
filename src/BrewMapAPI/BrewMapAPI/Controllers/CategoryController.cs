using BrewMapAPI.DTO.Category;
using BrewMapAPI.Service.Category;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace BrewMapAPI.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    [Authorize]
    public class CategoryController : ControllerBase
    {
        private readonly ICategoryService _service;

        public CategoryController(ICategoryService service)
        {
            _service = service;
        }

        [HttpGet]
        [Authorize(Roles = "admin,user")]
        public async Task<IActionResult> GetAll()
        {
            try
            {
                var categories = await _service.GetAllAsync();
                return Ok(categories);
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);
            }
        }

        [HttpGet("{tag}")]
        [Authorize(Roles = "admin,user")]
        public async Task<IActionResult> GetByTag(string tag)
        {
            try
            {
                var category = await _service.GetByTagAsync(tag);
                if (category == null)
                    return NotFound();

                return Ok(category);
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);
            }
        }

        [HttpPost]
        [Authorize(Roles = "admin")]
        public async Task<IActionResult> Create([FromBody] CreateCategory request)
        {
            try
            {
                if (!ModelState.IsValid)
                    return BadRequest(ModelState);

                var created = await _service.CreateAsync(request);
                return CreatedAtAction(nameof(GetByTag), new { tag = created.Tag }, created);
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);
            }
        }

        [HttpPut("{tag}")]
        [Authorize(Roles = "admin")]
        public async Task<IActionResult> Update(string tag, [FromBody] EditCategory request)
        {
            try
            {
                if (!ModelState.IsValid)
                    return BadRequest(ModelState);

                var updated = await _service.EditAsync(tag, request);
                if (updated == null)
                    return NotFound();

                return Ok(updated);
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);
            }
        }
    }
}
