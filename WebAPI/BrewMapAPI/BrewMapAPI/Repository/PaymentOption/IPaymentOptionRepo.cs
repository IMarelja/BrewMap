using BrewMapAPI.Models;

namespace BrewMapAPI.Repository.PaymentOptions
{
    public interface IPaymentOptionRepo
    {
        Task<PaymentOption?> GetByTagAsync(string tag);
    }
}
