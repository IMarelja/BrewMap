namespace BrewMapAPI.DTO.Pin
{
    public class ReadPin
    {
        public string Id { get; set; } = string.Empty;
        public double Latitude { get; set; }
        public double Longitude { get; set; }
    }
}