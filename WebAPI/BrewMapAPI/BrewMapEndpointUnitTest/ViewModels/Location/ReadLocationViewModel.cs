namespace BrewMapEndpointUnitTest.ViewModels.Location;

public class ReadLocationViewModel
{
    public string Id { get; set; } = String.Empty;
    public string Name { get; set; } = String.Empty;
    public string? Description { get; set; }
    public Address Address { get; set; } = new Address();
    public double Longitude { get; set; }
    public double Latitude { get; set; }
    public string CategoryTag { get; set; } = String.Empty;
    public List<string> PaymentOptionTags { get; set; } = new List<string>();
    public Dictionary<string, DayOpeningHours> OpeningHours { get; set; } = new Dictionary<string, DayOpeningHours>();
    public Contact? Contact { get; set; }
    public bool IsActive { get; set; }
    public double AverageRating { get; set; }
    public int TotalReviews { get; set; }
    public DateTime CreatedAt { get; set; }
}

public class DayOpeningHours
{
    public string? Open { get; set; }
    public string? Close { get; set; }
    public bool IsClosed { get; set; }
}

public class Address
{
    public string Street { get; set; } = String.Empty;
    public string City { get; set; } = String.Empty;
    public string Country { get; set; } = String.Empty;
    public string PostalCode { get; set; } = String.Empty;
}

public class Contact
{
    public string? Website { get; set; }
}
