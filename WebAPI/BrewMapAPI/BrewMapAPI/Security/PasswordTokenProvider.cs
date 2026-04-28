using System.Security.Cryptography;
using System.Text;

namespace BrewMapAPI.Security;

public class PasswordTokenProvider
{
    public static string GenerateSecureToken(int length = 64)
    {
        using (var rng = new RNGCryptoServiceProvider())
        {
            var tokenData = new byte[length];
            rng.GetBytes(tokenData);
            return Convert.ToBase64String(tokenData);
        }
    }
}