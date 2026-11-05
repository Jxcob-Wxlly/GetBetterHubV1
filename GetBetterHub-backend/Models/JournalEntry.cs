using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace GetBetterHub.Api.Models;

public enum SyncStatus
{
    Pending,
    Synced
}

public class JournalEntry
{
    [Key]
    public Guid EntryId { get; set; } = Guid.NewGuid();

    [Required]
    public Guid UserId { get; set; }

    [ForeignKey(nameof(UserId))]
    public User? User { get; set; }

    // URL into Azure Blob Storage once uploaded; null while only stored locally on-device.
    public string? AudioUrl { get; set; }

    public DateTime RecordedAt { get; set; } = DateTime.UtcNow;

    public SyncStatus SyncStatus { get; set; } = SyncStatus.Pending;

    // Optional short note the user attaches to the recording, e.g. "riff idea".
    [MaxLength(200)]
    public string? Note { get; set; }
}
