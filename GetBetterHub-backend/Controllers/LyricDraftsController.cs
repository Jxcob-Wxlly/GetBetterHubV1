using GetBetterHub.Api.Data;
using GetBetterHub.Api.DTOs;
using GetBetterHub.Api.Models;
using GetBetterHub.Api.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace GetBetterHub.Api.Controllers;

[ApiController]
[Route("api/lyric-drafts")]
[Authorize]
public class LyricDraftsController : ControllerBase
{
    private readonly AppDbContext _db;
    public LyricDraftsController(AppDbContext db) => _db = db;

    private static LyricDraftResponse ToResponse(LyricDraft d) => new()
    {
        DraftId = d.DraftId,
        Title = d.Title,
        Content = d.Content,
        LastEditedAt = d.LastEditedAt
    };

    [HttpGet]
    public async Task<ActionResult<List<LyricDraftResponse>>> GetAll()
    {
        var userId = User.GetUserId();
        var drafts = await _db.LyricDrafts
            .Where(d => d.UserId == userId)
            .OrderByDescending(d => d.LastEditedAt)
            .ToListAsync();
        return Ok(drafts.Select(ToResponse));
    }

    // Upsert by id: the Android client generates a UUID locally when a new draft is started
    // (so it can work offline), and this endpoint creates or updates using that same id.
    [HttpPut("{id}")]
    public async Task<ActionResult<LyricDraftResponse>> Save(Guid id, SaveLyricDraftRequest request)
    {
        var userId = User.GetUserId();
        var draft = await _db.LyricDrafts.FindAsync(id);

        if (draft == null)
        {
            draft = new LyricDraft { DraftId = id, UserId = userId };
            _db.LyricDrafts.Add(draft);
        }
        else if (draft.UserId != userId)
        {
            return Forbid();
        }

        draft.Title = request.Title;
        draft.Content = request.Content;
        draft.LastEditedAt = DateTime.UtcNow;

        await _db.SaveChangesAsync();
        return Ok(ToResponse(draft));
    }

    [HttpDelete("{id}")]
    public async Task<IActionResult> Delete(Guid id)
    {
        var draft = await _db.LyricDrafts.FindAsync(id);
        if (draft == null || draft.UserId != User.GetUserId()) return NotFound();
        _db.LyricDrafts.Remove(draft);
        await _db.SaveChangesAsync();
        return NoContent();
    }
}
