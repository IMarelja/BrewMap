using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using BrewMapAPI.Models;
using Microsoft.IdentityModel.Tokens;
using JwtRegisteredClaimNames = Microsoft.IdentityModel.JsonWebTokens.JwtRegisteredClaimNames;

namespace BrewMapAPI.Security;

public class JwtTokenProvider
{
    public static string CreateJwtToken(User user, IConfiguration configuration, int expiration = 60)
    {
        var secureKey = configuration["JWT:SecureKey"];
        var tokenKey = Encoding.UTF8.GetBytes(secureKey);
        var tokenDescriptor = new SecurityTokenDescriptor()
        {
            Expires = DateTime.Now.AddMinutes(expiration),
            SigningCredentials = new SigningCredentials(
                new SymmetricSecurityKey(tokenKey),
                SecurityAlgorithms.HmacSha256Signature)
        };
        if (!string.IsNullOrEmpty(user.Username))
        {
            tokenDescriptor.Subject = new ClaimsIdentity(new Claim[]
            {
                new Claim(ClaimTypes.NameIdentifier, user.Id),
                new Claim(ClaimTypes.Name, user.Username),
                new Claim(JwtRegisteredClaimNames.Sub, user.Username),
                new Claim(ClaimTypes.Role, user.Role)
            });
        }
        var tokenHandler = new JwtSecurityTokenHandler();
        var token = tokenHandler.CreateToken(tokenDescriptor);
        return tokenHandler.WriteToken(token);
    }
}