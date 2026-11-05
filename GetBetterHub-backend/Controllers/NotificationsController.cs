using GetBetterHub.Api.Data;
using GetBetterHub.Api.DTOs;
using GetBetterHub.Api.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace GetBetterHub.Api.Controllers;

[ApiController]
[Route("api/notifications")]
[Authorize]
public class NotificationsController : ControllerBase
{
    private readonly AppDbContext _db;
    public NotificationsController(AppDbContext db) => _db = db;

    /// <summary>The caller's latest 50 events. The phone de-duplicates by id, so repeated pulls are safe.</summary>
    [HttpGet]
    public async Task<ActionResult<List<NotificationResponse>>> GetAll()
    {
        var me = User.GetUserId();
        var items = await _db.Notifications
            .Where(n => n.UserId == me)
            .Include(n => n.Actor)
            .OrderByDescending(n => n.CreatedAt)
            .Take(50)
            .ToListAsync();

        return Ok(items.Select(n => new NotificationResponse
        {
            Id = n.NotificationId,
            Type = n.Type,
            ActorName = string.IsNullOrWhiteSpace(n.Actor!.DisplayName) ? n.Actor.Username : n.Actor.DisplayName!,
            CreatedAt = n.CreatedAt
        }).ToList());
    }
}
