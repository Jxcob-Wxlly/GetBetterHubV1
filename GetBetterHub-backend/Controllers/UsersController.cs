using System.Text.RegularExpressions;
using GetBetterHub.Api.Data;
using GetBetterHub.Api.DTOs;
using GetBetterHub.Api.Models;
using GetBetterHub.Api.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace GetBetterHub.Api.Controllers;

[ApiController]
[Route("api/users")]
[Authorize]
public class UsersController : ControllerBase
{
    private static readonly Regex HandleRegex = new("^[a-z0-9_]{3,20}$", RegexOptions.Compiled);
    private static readonly string[] Levels = { "beginner", "intermediate", "advanced" };

    private readonly AppDbContext _db;
    private readonly ILogger<UsersController> _log;

    public UsersController(AppDbContext db, ILogger<UsersController> log)
    {
        _db = db;
        _log = log;
    }

    // "me" rather than requiring the caller to pass their own id — the JWT already identifies them.
    [HttpGet("me")]
    public async Task<ActionResult<UserProfileResponse>> GetMe()
    {
        var userId = User.GetUserId();
        var user = await _db.Users.FindAsync(userId);
        if (user == null) return NotFound();

        var followers = await _db.Follows.CountAsync(f => f.FolloweeId == userId && f.Status == "accepted");
        var following = await _db.Follows.CountAsync(f => f.FollowerId == userId && f.Status == "accepted");

        return Ok(new UserProfileResponse
        {
            UserId = user.UserId,
            Username = user.Username,
            Email = user.Email,
            PreferredLanguage = user.PreferredLanguage,
            PrimaryInstrument = user.PrimaryInstrument,
            ProfileImageUrl = user.ProfileImageUrl,
            Bio = user.Bio,
            StreakCount = user.StreakCount,
            Level = user.Level,
            NotificationsEnabled = user.NotificationsEnabled,
            DisplayName = user.DisplayName,
            Handle = user.Handle,
            SkillLevel = user.SkillLevel,
            InstrumentsCsv = user.InstrumentsCsv,
            AvatarColorIndex = user.AvatarColorIndex,
            AvatarEmoji = user.AvatarEmoji,
            AvatarRound = user.AvatarRound,
            IsPrivate = user.IsPrivate,
            BioPublic = user.BioPublic,
            InstrumentsPublic = user.InstrumentsPublic,
            PicturePublic = user.PicturePublic,
            BadgesPublic = user.BadgesPublic,
            FollowerCount = followers,
            FollowingCount = following
        });
    }

    [HttpPut("me")]
    public async Task<ActionResult<UserProfileResponse>> UpdateMe(UpdateProfileRequest request)
    {
        var user = await _db.Users.FindAsync(User.GetUserId());
        if (user == null) return NotFound();

        if (request.Handle != null)
        {
            var handle = request.Handle.Trim().TrimStart('@').ToLowerInvariant();
            if (!HandleRegex.IsMatch(handle))
                return BadRequest("Handle must be 3-20 characters: letters, numbers or underscores.");
            var taken = await _db.Users.AnyAsync(u => u.Handle == handle && u.UserId != user.UserId);
            if (taken) return Conflict("That handle is already taken.");
            user.Handle = handle;
        }

        if (request.Bio != null) user.Bio = request.Bio;
        if (request.PreferredLanguage != null) user.PreferredLanguage = request.PreferredLanguage;
        if (request.PrimaryInstrument != null) user.PrimaryInstrument = request.PrimaryInstrument;
        if (request.ProfileImageUrl != null) user.ProfileImageUrl = request.ProfileImageUrl;
        if (request.NotificationsEnabled.HasValue) user.NotificationsEnabled = request.NotificationsEnabled.Value;
        if (request.DisplayName != null) user.DisplayName = request.DisplayName.Trim();
        if (request.SkillLevel != null && Levels.Contains(request.SkillLevel)) user.SkillLevel = request.SkillLevel;
        if (request.InstrumentsCsv != null) user.InstrumentsCsv = request.InstrumentsCsv;
        if (request.AvatarColorIndex.HasValue) user.AvatarColorIndex = request.AvatarColorIndex.Value;
        if (request.AvatarEmoji != null) user.AvatarEmoji = request.AvatarEmoji;
        if (request.AvatarRound.HasValue) user.AvatarRound = request.AvatarRound.Value;
        if (request.IsPrivate.HasValue) user.IsPrivate = request.IsPrivate.Value;
        if (request.BioPublic.HasValue) user.BioPublic = request.BioPublic.Value;
        if (request.InstrumentsPublic.HasValue) user.InstrumentsPublic = request.InstrumentsPublic.Value;
        if (request.PicturePublic.HasValue) user.PicturePublic = request.PicturePublic.Value;
        if (request.BadgesPublic.HasValue) user.BadgesPublic = request.BadgesPublic.Value;

        await _db.SaveChangesAsync();
        _log.LogInformation("Profile updated for {UserId}", user.UserId);
        return await GetMe();
    }
}
