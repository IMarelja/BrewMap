using System.ComponentModel.DataAnnotations;
using System.Linq;

namespace BrewMapAPI.Validation
{
    public class TargetTypeValidationAttribute : ValidationAttribute
    {
        private readonly string[] _validTypes = new[] { "location", "product" };

        protected override ValidationResult IsValid(object value, ValidationContext validationContext)
        {
            if (value is string str && _validTypes.Contains(str.ToLower()))
            {
                return ValidationResult.Success!;
            }
            return new ValidationResult("TargetType must be either 'location' or 'product'.");
        }
    }
}