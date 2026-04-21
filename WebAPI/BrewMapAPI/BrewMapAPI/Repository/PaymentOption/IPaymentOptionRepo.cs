using BrewMapAPI.Models;

namespace BrewMapAPI.Repository
{
    public interface IPaymentOptionRepo
    {
        Task<PaymentOption?> GetByTagAsync(string tag);
    }
}
