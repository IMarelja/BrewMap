using System.ComponentModel.DataAnnotations;

namespace BrewMapAPI.DTO.Location
{
    public class DeleteLocation
    {
        [Required]
        public string Id { get; set; }
    }
}