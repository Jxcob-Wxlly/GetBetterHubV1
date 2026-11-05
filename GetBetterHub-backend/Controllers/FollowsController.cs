using GetBetterHub.Api.Data;
using GetBetterHub.Api.DTOs;
using GetBetterHub.Api.Models;
using GetBetterHub.Api.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace GetBetterHub.Api.Controllers;

/// <summary>
/// Follow / unfollow, plus the approve/decline flow for private accounts. Each action that
/// another user should hear about also writes an <see cref="AppNotification"/> row, which the
/// phone pulls from GET api/notifications and raises as a system notification.
/// </summary>
[ApiController]
[Route("api/follows")]
[Authorize]
public class FollowsController : ControllerBase
{
    private readonly AppDbContext _db;
    private readonly ILogger<FollowsController> _log;

    public FollowsController(AppDbContext db, ILogger<FollowsController> log)
    {
        _db = db;
        _log = log;
    }

    [HttpPost("{userId:guid}")]
    public async Task<ActionResult<FollowResponse>> Follow(Guid userId)
    {
        var me = User.GetUserId();
        if (userId == me) return BadRequest("You can't follow yourself.");

        var target = await _db.Users.FindAsync(userId);
        if (target == null) return NotFound();

        var existing = await _db.Follows.FindAsync(me, userId);
        if (existing != null)
            return Ok(new FollowResponse { Status = existing.Status == "accepted" ? "following" : "requested" });

        var status = target.IsPrivate ? "pending" : "accepted";
        _db.Follows.Add(new Follow { FollowerId = me, FolloweeId = userId, Status = status });
        _db.Notifications.Add(new AppNotification
        {
            UserId = userId,
            ActorId = me,
            Type = target.IsPrivate ? "follow_request" : "new_follower"
        });
        await _db.SaveChangesAsync();
        _log.LogInformation("{Me} -> {Target}: {Status}", me, userId, status);
        return Ok(new FollowResponse { Status = target.IsPrivate ? "requested" : "following" });
    }

    [HttpDelete("{userId:guid}")]
    public async Task<IActionResult> Unfollow(Guid userId)
    {
        var existing = await _db.Follows.FindAsync(User.GetUserId(), userId);
        if (existing != null)
        {
            _db.Follows.Remove(existing);
            await _db.SaveChangesAsync();
        }
        return NoContent();
    }

    /// <summary>People waiting for the caller to approve their request to follow.</summary>
    [HttpGet("requests")]
    public async Task<ActionResult<List<PersonResponse>>> Requests()
    {
        var me = User.GetUserId();
        var pending = await _db.Follows
            .Where(f => f.FolloweeId == me && f.Status == "pending")
            .Include(f => f.Follower)
            .OrderByDescending(f => f.CreatedAt)
            .ToListAsync();

        return Ok(pending.Select(f => new PersonResponse
        {
            UserId = f.FollowerId,
            DisplayName = string.IsNullOrWhiteSpace(f.Follower!.DisplayName) ? f.Follower.Username : f.Follower.DisplayName!,
            Handle = f.Follower.Handle ?? string.Empty,
            Level = f.Follower.Level,
            IsPrivate = f.Follower.IsPrivate,
            Relationship = "none"
        }).ToList());
    }

    [HttpPost("requests/{userId:guid}/approve")]
    public async Task<IActionResult> Approve(Guid userId)
    {
        var me = User.GetUserId();
        var request = await _db.Follows.FindAsync(userId, me);
        if (request == null || request.Status != "pending") return NotFound();

        request.Status = "accepted";
        _db.Notifications.Add(new AppNotification { UserId = userId, ActorId = me, Type = "request_approved" });
        await _db.SaveChangesAsync();
        return NoContent();
    }

    [HttpPost("requests/{userId:guid}/decline")]
    public async Task<IActionResult> Decline(Guid userId)
    {
        var request = await _db.Follows.FindAsync(userId, User.GetUserId());
        if (request == null || request.Status != "pending") return NotFound();

        _db.Follows.Remove(request);
        await _db.SaveChangesAsync();
        return NoContent();
    }
}
