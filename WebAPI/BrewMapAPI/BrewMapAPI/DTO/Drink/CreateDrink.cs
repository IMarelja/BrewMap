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
        
        [Required]
        // ⚠️🪛 This line is purely for testing purposes (never should the id of the user be request in the api, use JWT when implemented)
        public string CreatedByUserId { get; set; } = string.Empty;
    }
}
