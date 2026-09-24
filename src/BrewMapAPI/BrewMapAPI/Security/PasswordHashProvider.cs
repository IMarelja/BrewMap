using System.Security.Cryptography;
using Microsoft.AspNetCore.Cryptography.KeyDerivation;

namespace BrewMapAPI.Security;

public class PasswordHashProvider
{
    public static string GetSalt()
    {
        byte[] salt = RandomNumberGenerator.GetBytes(128 / 8);
        return Convert.ToBase64String(salt);
    }

    public static string GetHash(string password, string b64salt)
    {
        byte[] salt = Convert.FromBase64String(b64salt);
        byte[] hash =
            KeyDerivation.Pbkdf2(
                password: password,
                salt: salt,
                prf: KeyDerivationPrf.HMACSHA256,
                iterationCount: 10000,
                numBytesRequested: 128 / 8);
        return Convert.ToBase64String(hash);
    }
}