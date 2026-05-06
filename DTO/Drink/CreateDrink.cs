using System.ComponentModel.DataAnnotations;

namespace BrewMapAPI.DTO.Drink
{
    public class CreateDrink
    {
        [Required]
        public string Name { get; set; } = string.Empty;
        public string? Description { get; set; }

        [Required]
        public string LocationId { get; set; } = string.Empty;
    }
}
