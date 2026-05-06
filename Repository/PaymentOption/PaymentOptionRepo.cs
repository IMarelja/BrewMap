using BrewMapAPI.Data;
using BrewMapAPI.DTO.PaymentOption;
using BrewMapAPI.Models;
using MongoDB.Driver;

namespace BrewMapAPI.Repository.PaymentOptions
{
    public class PaymentOptionRepo : IPaymentOptionRepo
    {
        private readonly IMongoCollection<PaymentOption> _paymentOptions;

        public PaymentOptionRepo(MongoDbContext context)
        {
            _paymentOptions = context.PaymentOptions;
        }

        public async Task<List<PaymentOption>> GetAllAsync()
        {
            return await _paymentOptions.Find(_ => true).ToListAsync();
        }

        public async Task<PaymentOption?> GetByTagAsync(string tag)
        {
            return await _paymentOptions
                .Find(p => p.Tag.ToLower() == tag.ToLower())
                .FirstOrDefaultAsync();
        }

        public async Task<PaymentOption> CreateAsync(CreatePaymentOption dto)
        {
            var normalizedTag = dto.Tag.ToLower();

            var paymentOption = new PaymentOption
            {
                Tag = normalizedTag,
                Name = dto.Name,
                IsActive = true,
                CreatedAt = DateTime.UtcNow,
                UpdatedAt = DateTime.UtcNow
            };

            await _paymentOptions.InsertOneAsync(paymentOption);
            return paymentOption;
        }

        public async Task<PaymentOption?> EditAsync(string tag, EditPaymentOption dto)
        {
            var normalizedTag = tag.ToLower();

            var update = Builders<PaymentOption>.Update
                .Set(p => p.Name, dto.Name)
                .Set(p => p.UpdatedAt, DateTime.UtcNow);

            var options = new FindOneAndUpdateOptions<PaymentOption>
            {
                ReturnDocument = ReturnDocument.After
            };

            return await _paymentOptions.FindOneAndUpdateAsync(
                p => p.Tag == normalizedTag,
                update,
                options
            );
        }

        public async Task<bool> DeleteAsync(string tag)
        {
            var normalizedTag = tag.ToLower();

            var result = await _paymentOptions.DeleteOneAsync(
                p => p.Tag == normalizedTag
            );

            return result.DeletedCount > 0;
        }
    }
}
