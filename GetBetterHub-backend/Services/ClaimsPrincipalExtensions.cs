using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;

namespace GetBetterHub.Api.Services;

public static class ClaimsPrincipalExtensions
{
    /// <summary>
    /// Reads the user id (JWT "sub" claim) out of the authenticated request. Every controller
    /// action other than register/login/google is [Authorize]-protected, so this is only ever
    /// called once the JWT bearer middleware has already validated the token.
    /// </summary>
    public static Guid GetUserId(this ClaimsPrincipal principal)
    {
        var sub = principal.FindFirstValue(JwtRegisteredClaimNames.Sub)
                  ?? principal.FindFirstValue(ClaimTypes.NameIdentifier);
        return Guid.Parse(sub!);
    }
}
