using System.ComponentModel.DataAnnotations;

namespace BrewMapAPI.DTO.Category
{
    public class CreateCategory
    {
        [Required]
        public string Tag { get; set; } = string.Empty;

        [Required]
        public string Name { get; set; } = string.Empty;
    }
}
