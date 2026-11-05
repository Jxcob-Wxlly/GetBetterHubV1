using GetBetterHub.Api.Data;
using GetBetterHub.Api.DTOs;
using GetBetterHub.Api.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace GetBetterHub.Api.Controllers;

[ApiController]
[Route("api/people")]
[Authorize]
public class PeopleController : ControllerBase
{
    private readonly AppDbContext _db;
    public PeopleController(AppDbContext db) => _db = db;

    /// <summary>Finds other users by display name, @handle or username (max 20 results).</summary>
    [HttpGet("search")]
    public async Task<ActionResult<List<PersonResponse>>> Search([FromQuery] string q)
    {
        var me = User.GetUserId();
        var term = (q ?? string.Empty).Trim().TrimStart('@').ToLowerInvariant();
        if (term.Length < 1) return Ok(new List<PersonResponse>());

        var users = await _db.Users
            .Where(u => u.UserId != me &&
                (u.Username.ToLower().Contains(term) ||
                 (u.DisplayName != null && u.DisplayName.ToLower().Contains(term)) ||
                 (u.Handle != null && u.Handle.Contains(term))))
            .OrderBy(u => u.Username)
            .Take(20)
            .ToListAsync();

        var ids = users.Select(u => u.UserId).ToList();
        var mine = await _db.Follows
            .Where(f => f.FollowerId == me && ids.Contains(f.FolloweeId))
            .ToDictionaryAsync(f => f.FolloweeId, f => f.Status);

        return Ok(users.Select(u => new PersonResponse
        {
            UserId = u.UserId,
            DisplayName = string.IsNullOrWhiteSpace(u.DisplayName) ? u.Username : u.DisplayName!,
            Handle = u.Handle ?? string.Empty,
            Level = u.Level,
            IsPrivate = u.IsPrivate,
            Relationship = mine.TryGetValue(u.UserId, out var status)
                ? (status == "accepted" ? "following" : "requested")
                : "none"
        }).ToList());
    }
}
