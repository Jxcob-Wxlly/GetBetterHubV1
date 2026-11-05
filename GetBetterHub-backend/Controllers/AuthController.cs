using GetBetterHub.Api.Data;
using GetBetterHub.Api.DTOs;
using GetBetterHub.Api.Models;
using GetBetterHub.Api.Services;
using Google.Apis.Auth;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace GetBetterHub.Api.Controllers;

[ApiController]
[Route("api/auth")]
public class AuthController : ControllerBase
{
    private readonly AppDbContext _db;
    private readonly JwtService _jwt;
    private readonly IConfiguration _config;

    public AuthController(AppDbContext db, JwtService jwt, IConfiguration config)
    {
        _db = db;
        _jwt = jwt;
        _config = config;
    }

    [HttpPost("register")]
    public async Task<ActionResult<AuthResponse>> Register(RegisterRequest request)
    {
        if (await _db.Users.AnyAsync(u => u.Email == request.Email))
            return Conflict(new { message = "An account with this email already exists." });

        if (await _db.Users.AnyAsync(u => u.Username == request.Username))
            return Conflict(new { message = "That username is already taken." });

        var user = new User
        {
            Username = request.Username,
            Email = request.Email,
            // Passwords are never stored in plain text — BCrypt salts and hashes before persisting.
            PasswordHash = BCrypt.Net.BCrypt.HashPassword(request.Password),
            PreferredLanguage = request.PreferredLanguage,
            PrimaryInstrument = request.PrimaryInstrument
        };

        _db.Users.Add(user);
        await _db.SaveChangesAsync();

        var (token, expiresAt) = _jwt.GenerateToken(user);
        return Ok(new AuthResponse { Token = token, ExpiresAt = expiresAt, UserId = user.UserId, Username = user.Username });
    }

    [HttpPost("login")]
    public async Task<ActionResult<AuthResponse>> Login(LoginRequest request)
    {
        var user = await _db.Users.FirstOrDefaultAsync(u => u.Email == request.Email);

        // Same "invalid email or password" message whether the email doesn't exist or the
        // password is wrong — never reveal which one it was, that's an account-enumeration leak.
        if (user == null || user.PasswordHash == null || !BCrypt.Net.BCrypt.Verify(request.Password, user.PasswordHash))
            return Unauthorized(new { message = "Invalid email or password." });

        var (token, expiresAt) = _jwt.GenerateToken(user);
        return Ok(new AuthResponse { Token = token, ExpiresAt = expiresAt, UserId = user.UserId, Username = user.Username });
    }

    [HttpPost("google")]
    public async Task<ActionResult<AuthResponse>> GoogleLogin(GoogleLoginRequest request)
    {
        GoogleJsonWebSignature.Payload payload;
        try
        {
            var validationSettings = new GoogleJsonWebSignature.ValidationSettings
            {
                // Restrict to this app's own OAuth client ID so a token minted for a different
                // app can't be replayed against this API.
                Audience = new[] { _config["Google:ClientId"] }
            };
            payload = await GoogleJsonWebSignature.ValidateAsync(request.IdToken, validationSettings);
        }
        catch (InvalidJwtException)
        {
            return Unauthorized(new { message = "Invalid Google sign-in token." });
        }

        var user = await _db.Users.FirstOrDefaultAsync(u => u.GoogleId == payload.Subject || u.Email == payload.Email);

        if (user == null)
        {
            // First time this Google account has signed in — provision a new account automatically.
            user = new User
            {
                Username = payload.Email.Split('@')[0],
                Email = payload.Email,
                GoogleId = payload.Subject,
                ProfileImageUrl = payload.Picture
            };
            _db.Users.Add(user);
            await _db.SaveChangesAsync();
        }
        else if (user.GoogleId == null)
        {
            // Existing email/password account signing in with Google for the first time — link it.
            user.GoogleId = payload.Subject;
            await _db.SaveChangesAsync();
        }

        var (token, expiresAt) = _jwt.GenerateToken(user);
        return Ok(new AuthResponse { Token = token, ExpiresAt = expiresAt, UserId = user.UserId, Username = user.Username });
    }
}
