using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using Microsoft.IdentityModel.Tokens;
using JwtRegisteredClaimNames = Microsoft.IdentityModel.JsonWebTokens.JwtRegisteredClaimNames;

namespace BrewMapAPI.Security;

public class JwtTokenProvider
{
    public static string CreateJwtToken(string secureKey, int expiration, string subject = null, string role = null)
    {
        var tokenKey = Encoding.UTF8.GetBytes(secureKey);
        var tokenDescriptor = new SecurityTokenDescriptor()
        {
            Expires = DateTime.Now.AddMinutes(expiration),
            SigningCredentials = new SigningCredentials(
                new SymmetricSecurityKey(tokenKey),
                SecurityAlgorithms.HmacSha256Signature)
        };
        if (!string.IsNullOrEmpty(subject))
        {
            tokenDescriptor.Subject = new ClaimsIdentity(new Claim[]
            {
                new Claim(ClaimTypes.Name, subject),
                new Claim(JwtRegisteredClaimNames.Sub, subject),
                new Claim(ClaimTypes.Role, role)
            });
        }
        var tokenHandler = new JwtSecurityTokenHandler();
        var token = tokenHandler.CreateToken(tokenDescriptor);
        return tokenHandler.WriteToken(token);
    }
}