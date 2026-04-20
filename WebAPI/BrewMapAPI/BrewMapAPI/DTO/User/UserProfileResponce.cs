namespace BrewMapAPI.DTO.User
{
    public class UserProfileResponce
    {
        public int StatusCode { get; set; } = 500;
        public bool Success { get; set; } = false;
        public string Message { get; set; } = string.Empty;
    }
}
