using BrewMapAPI.Models;

namespace BrewMapAPI.Repository.Locations
{
    public interface IPaymentOptionRepo
    {
        Task<PaymentOption?> GetByTagAsync(string tag);
    }
}
