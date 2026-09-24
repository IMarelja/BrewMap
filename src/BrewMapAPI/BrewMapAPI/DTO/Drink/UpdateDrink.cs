using System.ComponentModel.DataAnnotations;

namespace BrewMapAPI.DTO.Drink
{
    public class UpdateDrink
    {
        
        [Required]
        public string? Name { get; set; }
        
        public string? Description { get; set; }
    }
}
