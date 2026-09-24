
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace BrewMapAPI.Controllers
{
    [Route("api/")]
    [ApiController]

    public class StatusController : ControllerBase
    {
        [HttpGet("isAlive")]
        [AllowAnonymous]
        public async Task<IActionResult> isAlive()
        {
            return Ok();
        }
    }
}