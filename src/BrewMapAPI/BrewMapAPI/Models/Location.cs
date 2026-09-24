using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;
using MongoDB.Driver.GeoJsonObjectModel;
using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Text.RegularExpressions;

namespace BrewMapAPI.Models
{
    public class Location
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string Id { get; set; } = String.Empty;

        [BsonElement("name")]
        public string Name { get; set; } = String.Empty;

        [BsonElement("description")]
        public string? Description { get; set; }

        [BsonElement("address")]
        public Address Address { get; set; } = new Address();

        [BsonElement("location")]
        public GeoJsonPoint<GeoJson2DGeographicCoordinates> LocationPoint { get; set; } = new GeoJsonPoint<GeoJson2DGeographicCoordinates>(new GeoJson2DGeographicCoordinates(0, 0));

        [BsonElement("categoryTag")]
        public string CategoryTag { get; set; }  = String.Empty;

        [BsonElement("paymentOptionTags")]
        public List<string> PaymentOptionTags { get; set; } = new List<string>();

        [BsonElement("openingHours")]
        public Dictionary<string, DayOpeningHours> OpeningHours { get; set; } = new Dictionary<string, DayOpeningHours>();

        [BsonElement("contact")]
        public Contact? Contact { get; set; }

        [BsonElement("reportCount")]
        public int ReportCount { get; set; } = 0;

        [BsonElement("isActive")]
        public bool IsActive { get; set; } = true;

        [BsonElement("addedByUserId")]
        [BsonRepresentation(BsonType.ObjectId)]
        public string AddedByUserId { get; set; } = String.Empty;

        [BsonElement("createdAt")]
        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

        [BsonElement("updatedAt")]
        public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;

        [BsonElement("edits")]
        public List<EditHistory> Edits { get; set; } = new List<EditHistory>();

        [BsonElement("aggregatedRating")]
        public AggregatedRating AggregatedRating { get; set; } = new AggregatedRating();
    }

    public class Address
    {
        [BsonElement("street")]
        public string Street { get; set; } = String.Empty;

        [BsonElement("city")]
        public string City { get; set; } = String.Empty;

        [BsonElement("country")]
        public string Country { get; set; } = String.Empty;

        [BsonElement("postalCode")]
        public string PostalCode { get; set; } = String.Empty;
    }

    public class DayOpeningHours : IValidatableObject
    {
        private static readonly Regex TimeRegex =
            new(@"^([01]\d|2[0-3]):([0-5]\d)$", RegexOptions.Compiled);

        [BsonElement("open")]
        public string? Open { get; set; }

        [BsonElement("close")]
        public string? Close { get; set; }

        [BsonElement("isClosed")]
        public bool IsClosed { get; set; }

        public IEnumerable<ValidationResult> Validate(ValidationContext validationContext)
        {
            if (IsClosed)
            {
                // ignore Swagger "string"
                if (!string.IsNullOrWhiteSpace(Open) && Open.ToLower() != "string")
                {
                    yield return new ValidationResult(
                        "Open must be null when IsClosed is true.",
                        new[] { nameof(Open) });
                }

                if (!string.IsNullOrWhiteSpace(Close) && Close.ToLower() != "string")
                {
                    yield return new ValidationResult(
                        "Close must be null when IsClosed is true.",
                        new[] { nameof(Close) });
                }

                yield break;
            }
            else
            {
                if (string.IsNullOrWhiteSpace(Open) || Open.ToLower() == "string" ||
                    string.IsNullOrWhiteSpace(Close) || Close.ToLower() == "string")
                {
                    yield return new ValidationResult(
                        "Open and Close are required when IsClosed is false.",
                        new[] { nameof(Open), nameof(Close) });
                }

                if (!TimeRegex.IsMatch(Open ?? ""))
                {
                    yield return new ValidationResult(
                        "Open time must be in HH:mm format.",
                        new[] { nameof(Open) });
                }

                if (!TimeRegex.IsMatch(Close ?? ""))
                {
                    yield return new ValidationResult(
                        "Close time must be in HH:mm format.",
                        new[] { nameof(Close) });
                }
            }
        }
    }

    public class Contact
    {
        [BsonElement("website")]
        public string? Website { get; set; }
    }

    public class EditHistory
    {
        [BsonElement("editedByUserId")]
        [BsonRepresentation(BsonType.ObjectId)]
        public string EditedByUserId { get; set; }  = String.Empty;

        [BsonElement("editedAt")]
        public DateTime EditedAt { get; set; }

        [BsonElement("editComment")]
        public string? EditComment { get; set; }
    }

}