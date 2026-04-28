using BrewMapAPI.DTO.PaymentOption;
using BrewMapAPI.Repository.PaymentOptions;

namespace BrewMapAPI.Service.PaymentOption
{
    public class PaymentOptionService : IPaymentOptionService
    {
        private readonly IPaymentOptionRepo _repo;

        public PaymentOptionService(IPaymentOptionRepo repo)
        {
            _repo = repo;
        }

        public async Task<List<ReadPaymentOption>> GetAllAsync()
        {
            var paymentOptions = await _repo.GetAllAsync();
            return paymentOptions.Select(ToReadDto).ToList();
        }

        public async Task<ReadPaymentOption?> GetByTagAsync(string tag)
        {
            var paymentOption = await _repo.GetByTagAsync(tag);
            if (paymentOption == null)
                return null;

            return ToReadDto(paymentOption);
        }

        public async Task<ReadPaymentOption> CreateAsync(CreatePaymentOption dto)
        {
            dto.Tag = dto.Tag.Trim().ToLower();
            dto.Name = dto.Name.Trim();

            var existing = await _repo.GetByTagAsync(dto.Tag);
            if (existing != null)
                throw new InvalidOperationException($"A payment option with tag '{dto.Tag}' already exists.");

            var created = await _repo.CreateAsync(dto);
            return ToReadDto(created);
        }

        public async Task<ReadPaymentOption?> EditAsync(string tag, EditPaymentOption dto)
        {
            dto.Name = dto.Name.Trim();

            var updated = await _repo.EditAsync(tag, dto);
            if (updated == null)
                return null;

            return ToReadDto(updated);
        }

        public async Task<bool> DeleteAsync(string tag)
        {
            return await _repo.DeleteAsync(tag);
        }

        private ReadPaymentOption ToReadDto(Models.PaymentOption model)
        {
            return new ReadPaymentOption
            {
                Tag = model.Tag,
                Name = model.Name
            };
        }
    }
}
