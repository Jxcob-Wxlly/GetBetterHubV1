using GetBetterHub.Api.Data;
using GetBetterHub.Api.DTOs;
using GetBetterHub.Api.Models;
using GetBetterHub.Api.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace GetBetterHub.Api.Controllers;

[ApiController]
[Route("api/journal-entries")]
[Authorize]
public class JournalEntriesController : ControllerBase
{
    private readonly AppDbContext _db;
    public JournalEntriesController(AppDbContext db) => _db = db;

    private static JournalEntryResponse ToResponse(JournalEntry e) => new()
    {
        EntryId = e.EntryId,
        AudioUrl = e.AudioUrl,
        Note = e.Note,
        RecordedAt = e.RecordedAt,
        SyncStatus = e.SyncStatus.ToString()
    };

    [HttpGet]
    public async Task<ActionResult<List<JournalEntryResponse>>> GetAll()
    {
        var userId = User.GetUserId();
        var entries = await _db.JournalEntries
            .Where(e => e.UserId == userId)
            .OrderByDescending(e => e.RecordedAt)
            .ToListAsync();
        return Ok(entries.Select(ToResponse));
    }

    [HttpPost]
    public async Task<ActionResult<JournalEntryResponse>> Create(CreateJournalEntryRequest request)
    {
        var entry = new JournalEntry
        {
            UserId = User.GetUserId(),
            AudioUrl = request.AudioUrl,
            Note = request.Note,
            // Arriving via this endpoint at all means the client already has the file somewhere
            // reachable (uploaded to Blob Storage first); mark it synced immediately.
            SyncStatus = SyncStatus.Synced
        };
        _db.JournalEntries.Add(entry);
        await _db.SaveChangesAsync();
        return Ok(ToResponse(entry));
    }

    [HttpDelete("{id}")]
    public async Task<IActionResult> Delete(Guid id)
    {
        var entry = await _db.JournalEntries.FindAsync(id);
        if (entry == null || entry.UserId != User.GetUserId()) return NotFound();
        _db.JournalEntries.Remove(entry);
        await _db.SaveChangesAsync();
        return NoContent();
    }
}
