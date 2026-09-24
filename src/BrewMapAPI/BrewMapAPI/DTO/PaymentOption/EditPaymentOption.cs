using System.ComponentModel.DataAnnotations;

namespace BrewMapAPI.DTO.PaymentOption
{
    public class EditPaymentOption
    {
        [Required]
        public string Name { get; set; } = string.Empty;
    }
}
