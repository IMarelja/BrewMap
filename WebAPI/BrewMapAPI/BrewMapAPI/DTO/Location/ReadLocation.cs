using BrewMapAPI.Models;
using Microsoft.AspNetCore.Mvc;
using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;

namespace BrewMapAPI.DTO.Location
{
    public class ReadLocation
    {
        public string Id { get; set; }
        public string Name { get; set; }
        public string? Description { get; set; }
        public Address Address { get; set; }
        public double Latitude { get; set; }
        public double Longitude { get; set; }
        public string CategoryTag { get; set; }
        public List<string> PaymentOptionTags { get; set; }
        public Dictionary<string, DayOpeningHours> OpeningHours { get; set; }
        public Contact? Contact { get; set; }
        public bool IsActive { get; set; }
        public double AverageRating { get; set; }
        public int TotalReviews { get; set; }
        public DateTime CreatedAt { get; set; }
    }

    public class SearchLocationQuery
    {
        [FromQuery(Name = "query")]
        public string? Query { get; set; }

        [FromQuery]
        public List<string>? PaymentOptionTags { get; set; }

        [FromQuery]
        public double? MinRating { get; set; }

        [FromQuery]
        public string? DrinkType { get; set; }

        [FromQuery]
        public double? Latitude { get; set; }

        [FromQuery]
        public double? Longitude { get; set; }

        [FromQuery]
        public double RadiusMeters { get; set; } = 3000;
    }
}