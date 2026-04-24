using BrewMapAPI.Models;
using Microsoft.AspNetCore.Mvc;
using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;

namespace BrewMapAPI.DTO.Location
{
    public class ReadLocation
    {
        public string Id { get; set; } = String.Empty;
        public string Name { get; set; } = String.Empty;
        public string? Description { get; set; }
        public Address Address { get; set; } = new Address();
        public double Latitude { get; set; }
        public double Longitude { get; set; }
        public string CategoryTag { get; set; } = String.Empty;
        public List<string> PaymentOptionTags { get; set; } = new List<string>();
        public Dictionary<string, DayOpeningHours> OpeningHours { get; set; } = new Dictionary<string, DayOpeningHours>();
        public Contact? Contact { get; set; }
        public bool IsActive { get; set; }
        public double AverageRating { get; set; }
        public int TotalReviews { get; set; }
        public DateTime CreatedAt { get; set; }
    }
}