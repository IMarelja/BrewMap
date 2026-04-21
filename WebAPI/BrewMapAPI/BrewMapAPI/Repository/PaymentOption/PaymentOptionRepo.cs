using BrewMapAPI.Data;
using BrewMapAPI.Models;
using MongoDB.Driver;

namespace BrewMapAPI.Repository
{
    public class PaymentOptionRepo : IPaymentOptionRepo
    {
        private readonly IMongoCollection<PaymentOption> _paymentOptions;

        public PaymentOptionRepo(MongoDbContext context)
        {
            _paymentOptions = context.PaymentOptions;
        }

        public async Task<PaymentOption?> GetByTagAsync(string tag)
        {
            return await _paymentOptions
                .Find(p => p.Tag.ToLower() == tag.ToLower())
                .FirstOrDefaultAsync();
        }
    }
}
