//Ths is just so that I can test the connection ti the database without having to write the code for each controller.
using Microsoft.AspNetCore.Http;
using BrewMapAPI.Service.User;            
using Microsoft.AspNetCore.Mvc;

namespace BrewMapAPI.Controllers
{
    [ApiController]
    [Route("[controller]")]
    public class HelloController : ControllerBase
    {
        [HttpGet]
        public IActionResult Get() => Ok("hello world");
    }
}