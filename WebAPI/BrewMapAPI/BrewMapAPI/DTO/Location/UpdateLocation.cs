using BrewMapAPI.Models;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;

namespace BrewMapAPI.DTO.Location
{
    public class UpdateLocation
    {
        [Required]
        public string Id { get; set; }

        public string Name { get; set; }
        public string? Description { get; set; }
        public Address Address { get; set; }
        public double? Latitude { get; set; }
        public double? Longitude { get; set; }
        public string CategoryTag { get; set; }
        public List<string> PaymentOptionTags { get; set; }
        public Dictionary<string, DayOpeningHours> OpeningHours { get; set; }
        public Contact Contact { get; set; }
        public string? EditComment { get; set; }
    }
}