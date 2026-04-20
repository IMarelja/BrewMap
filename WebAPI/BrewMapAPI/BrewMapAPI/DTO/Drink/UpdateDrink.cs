using System.ComponentModel.DataAnnotations;

namespace BrewMapAPI.DTO.Drink
{
    public class UpdateDrink
    {
        [Required]
        public string Id { get; set; } = string.Empty;
        
        [Required]
        public string? Name { get; set; }
        
        public string? Description { get; set; }
    }
}
