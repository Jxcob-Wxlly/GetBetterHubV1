using GetBetterHub.Api.Data;
using GetBetterHub.Api.DTOs;
using GetBetterHub.Api.Models;
using GetBetterHub.Api.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace GetBetterHub.Api.Controllers;

[ApiController]
[Route("api/practice-sessions")]
[Authorize]
public class PracticeSessionsController : ControllerBase
{
    private readonly AppDbContext _db;
    public PracticeSessionsController(AppDbContext db) => _db = db;

    [HttpGet]
    public async Task<ActionResult<List<PracticeSessionResponse>>> GetAll()
    {
        var userId = User.GetUserId();
        var sessions = await _db.PracticeSessions
            .Where(s => s.UserId == userId)
            .OrderByDescending(s => s.CompletedAt)
            .Take(50)
            .Select(s => new PracticeSessionResponse
            {
                SessionId = s.SessionId,
                Instrument = s.Instrument,
                DrillType = s.DrillType,
                Mood = s.Mood,
                DurationSeconds = s.DurationSeconds,
                CompletedAt = s.CompletedAt
            })
            .ToListAsync();

        return Ok(sessions);
    }

    [HttpPost]
    public async Task<ActionResult<PracticeSessionResponse>> Create(CreatePracticeSessionRequest request)
    {
        var userId = User.GetUserId();
        var session = new PracticeSession
        {
            UserId = userId,
            Instrument = request.Instrument,
            DrillType = request.DrillType,
            Mood = request.Mood,
            DurationSeconds = request.DurationSeconds
        };
        _db.PracticeSessions.Add(session);

        // Practice completion is also where streak/level/XP live server-side, so the leaderboard
        // and badges reflect the same numbers regardless of which device the user is on.
        var user = await _db.Users.FindAsync(userId);
        if (user != null)
        {
            var today = DateOnly.FromDateTime(DateTime.UtcNow);
            var lastSession = await _db.PracticeSessions
                .Where(s => s.UserId == userId)
                .OrderByDescending(s => s.CompletedAt)
                .Select(s => s.CompletedAt)
                .FirstOrDefaultAsync();

            var lastDay = lastSession == default ? (DateOnly?)null : DateOnly.FromDateTime(lastSession);
            user.StreakCount = lastDay switch
            {
                null => 1,
                var d when d == today => user.StreakCount,
                var d when d == today.AddDays(-1) => user.StreakCount + 1,
                _ => 1
            };
        }

        await _db.SaveChangesAsync();

        return Ok(new PracticeSessionResponse
        {
            SessionId = session.SessionId,
            Instrument = session.Instrument,
            DrillType = session.DrillType,
            Mood = session.Mood,
            DurationSeconds = session.DurationSeconds,
            CompletedAt = session.CompletedAt
        });
    }
}
