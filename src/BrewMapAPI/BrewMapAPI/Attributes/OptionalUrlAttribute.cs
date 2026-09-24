using System.ComponentModel.DataAnnotations;

namespace BrewMapAPI.Attributes
{
    public class OptionalUrlAttribute : ValidationAttribute
    {
        public override bool IsValid(object? value)
        {
            if (value == null || string.IsNullOrWhiteSpace(value as string))
                return true;
            return new UrlAttribute().IsValid(value);
        }
    }
}
