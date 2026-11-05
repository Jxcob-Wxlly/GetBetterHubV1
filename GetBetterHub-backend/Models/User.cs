using System.ComponentModel.DataAnnotations;

namespace GetBetterHub.Api.Models;

public class User
{
    [Key]
    public Guid UserId { get; set; } = Guid.NewGuid();

    [Required, MaxLength(50)]
    public string Username { get; set; } = string.Empty;

    [Required, MaxLength(255)]
    public string Email { get; set; } = string.Empty;

    // Nullable: a user who registered via Google SSO may never set a local password.
    public string? PasswordHash { get; set; }

    // Set when the account was created via Google SSO, used to avoid duplicate accounts.
    public string? GoogleId { get; set; }

    [MaxLength(5)]
    public string PreferredLanguage { get; set; } = "en"; // en | zu | af

    [MaxLength(50)]
    public string? PrimaryInstrument { get; set; }

    public string? ProfileImageUrl { get; set; }

    public string? Bio { get; set; }

    public int StreakCount { get; set; } = 0;

    public int Level { get; set; } = 1;

    public bool NotificationsEnabled { get; set; } = true;

    // ---- Final-build profile fields (all optional so older rows/clients keep working) ----
    [MaxLength(50)]
    public string? DisplayName { get; set; }

    // Unique @handle, stored lowercase without the "@" (3-20 chars: a-z, 0-9, underscore).
    [MaxLength(20)]
    public string? Handle { get; set; }

    [MaxLength(20)]
    public string SkillLevel { get; set; } = "beginner"; // beginner | intermediate | advanced

    // Comma-separated instrument keys the user practises, e.g. "vocals,guitar".
    public string? InstrumentsCsv { get; set; }

    public int AvatarColorIndex { get; set; } = 0;
    [MaxLength(8)]
    public string? AvatarEmoji { get; set; }
    public bool AvatarRound { get; set; } = true;

    // Privacy: a private account must approve follow requests; the *_Public flags control what others see.
    public bool IsPrivate { get; set; } = false;
    public bool BioPublic { get; set; } = true;
    public bool InstrumentsPublic { get; set; } = true;
    public bool PicturePublic { get; set; } = true;
    public bool BadgesPublic { get; set; } = true;

    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

    // Navigation properties
    public List<PracticeSession> PracticeSessions { get; set; } = new();
    public List<JournalEntry> JournalEntries { get; set; } = new();
    public List<LyricDraft> LyricDrafts { get; set; } = new();
    public List<PerformancePost> PerformancePosts { get; set; } = new();
    public List<Comment> Comments { get; set; } = new();
}
