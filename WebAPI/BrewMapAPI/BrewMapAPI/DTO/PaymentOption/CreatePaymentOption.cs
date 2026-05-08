using System.ComponentModel.DataAnnotations;

namespace BrewMapAPI.DTO.PaymentOption
{
    public class CreatePaymentOption
    {
        [Required]
        public string Tag { get; set; } = string.Empty;

        [Required]
        public string Name { get; set; } = string.Empty;
    }
}
