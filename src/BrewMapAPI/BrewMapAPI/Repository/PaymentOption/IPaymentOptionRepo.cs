using BrewMapAPI.DTO.PaymentOption;
using BrewMapAPI.Models;

namespace BrewMapAPI.Repository.PaymentOptions
{
    public interface IPaymentOptionRepo
    {
        Task<PaymentOption?> GetByTagAsync(string tag);
        Task<List<PaymentOption>> GetAllAsync();
        Task<PaymentOption> CreateAsync(CreatePaymentOption dto);
        Task<PaymentOption?> EditAsync(string tag, EditPaymentOption dto);
        Task<bool> DeleteAsync(string tag);
    }
}
