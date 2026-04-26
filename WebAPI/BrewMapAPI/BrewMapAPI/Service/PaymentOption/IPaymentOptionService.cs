using BrewMapAPI.DTO.PaymentOption;

namespace BrewMapAPI.Service.PaymentOption
{
    public interface IPaymentOptionService
    {
        Task<List<ReadPaymentOption>> GetAllAsync();
        Task<ReadPaymentOption?> GetByTagAsync(string tag);
        Task<ReadPaymentOption> CreateAsync(CreatePaymentOption dto);
        Task<ReadPaymentOption?> EditAsync(string tag, EditPaymentOption dto);
        Task<bool> DeleteAsync(string tag);
    }
}
