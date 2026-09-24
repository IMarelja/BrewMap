using System.ComponentModel.DataAnnotations;

namespace BrewMapAPI.DTO.Category
{
    public class EditCategory
    {
        [Required]
        public string Name { get; set; } = string.Empty;
    }
}
