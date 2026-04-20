using BrewMapAPI.Models;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;

namespace BrewMapAPI.DTO.Location
{
    public class CreateLocation
    {
        [Required]
        public string Name { get; set; }

        public string? Description { get; set; }

        [Required]
        public Address Address { get; set; }

        [Required]
        public double Latitude { get; set; }

        [Required]
        public double Longitude { get; set; }

        public string CategoryTag { get; set; } = "Cafe";

        public List<string> PaymentOptionTags { get; set; } = new();

        public Dictionary<string, DayOpeningHours> OpeningHours { get; set; } = new();

        public Contact Contact { get; set; }
    }
}