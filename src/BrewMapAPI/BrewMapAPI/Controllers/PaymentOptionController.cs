using BrewMapAPI.DTO.PaymentOption;
using BrewMapAPI.Service.PaymentOption;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace BrewMapAPI.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    [Authorize]
    public class PaymentOptionController : ControllerBase
    {
        private readonly IPaymentOptionService _service;

        public PaymentOptionController(IPaymentOptionService service)
        {
            _service = service;
        }

        [HttpGet]
        [Authorize(Roles = "admin,user")]
        public async Task<IActionResult> GetAll()
        {
            try
            {
                var paymentOptions = await _service.GetAllAsync();
                return Ok(paymentOptions);
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
                var paymentOption = await _service.GetByTagAsync(tag);
                if (paymentOption == null)
                    return NotFound();

                return Ok(paymentOption);
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);
            }
        }

        [HttpPost]
        [Authorize(Roles = "admin")]
        public async Task<IActionResult> Create([FromBody] CreatePaymentOption request)
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
        public async Task<IActionResult> Update(string tag, [FromBody] EditPaymentOption request)
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
