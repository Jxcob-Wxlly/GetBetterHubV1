using System.ComponentModel.DataAnnotations;

namespace GetBetterHub.Api.DTOs;

public class UserProfileResponse
{
    public Guid UserId { get; set; }
    public string Username { get; set; } = string.Empty;
    public string Email { get; set; } = string.Empty;
    public string PreferredLanguage { get; set; } = "en";
    public string? PrimaryInstrument { get; set; }
    public string? ProfileImageUrl { get; set; }
    public string? Bio { get; set; }
    public int StreakCount { get; set; }
    public int Level { get; set; }
    public bool NotificationsEnabled { get; set; }
    public string? DisplayName { get; set; }
    public string? Handle { get; set; }
    public string SkillLevel { get; set; } = "beginner";
    public string? InstrumentsCsv { get; set; }
    public int AvatarColorIndex { get; set; }
    public string? AvatarEmoji { get; set; }
    public bool AvatarRound { get; set; } = true;
    public bool IsPrivate { get; set; }
    public bool BioPublic { get; set; } = true;
    public bool InstrumentsPublic { get; set; } = true;
    public bool PicturePublic { get; set; } = true;
    public bool BadgesPublic { get; set; } = true;
    public int FollowerCount { get; set; }
    public int FollowingCount { get; set; }
}

public class UpdateProfileRequest
{
    public string? Bio { get; set; }
    public string? PreferredLanguage { get; set; }
    public string? PrimaryInstrument { get; set; }
    public string? ProfileImageUrl { get; set; }
    public bool? NotificationsEnabled { get; set; }
    [MaxLength(50)] public string? DisplayName { get; set; }
    public string? Handle { get; set; }
    public string? SkillLevel { get; set; }
    public string? InstrumentsCsv { get; set; }
    public int? AvatarColorIndex { get; set; }
    public string? AvatarEmoji { get; set; }
    public bool? AvatarRound { get; set; }
    public bool? IsPrivate { get; set; }
    public bool? BioPublic { get; set; }
    public bool? InstrumentsPublic { get; set; }
    public bool? PicturePublic { get; set; }
    public bool? BadgesPublic { get; set; }
}

/// <summary>A person in search results or the follow-request list, with the caller's relationship to them.</summary>
public class PersonResponse
{
    public Guid UserId { get; set; }
    public string DisplayName { get; set; } = string.Empty;
    public string Handle { get; set; } = string.Empty;
    public int Level { get; set; }
    public bool IsPrivate { get; set; }
    /// <summary>none | following | requested</summary>
    public string Relationship { get; set; } = "none";
}

public class FollowResponse
{
    /// <summary>"following" or "requested" (private account).</summary>
    public string Status { get; set; } = "following";
}

public class NotificationResponse
{
    public Guid Id { get; set; }
    public string Type { get; set; } = string.Empty;
    public string ActorName { get; set; } = string.Empty;
    public DateTime CreatedAt { get; set; }
}

public class CreatePracticeSessionRequest
{
    [Required] public string Instrument { get; set; } = string.Empty;
    public string? DrillType { get; set; }
    public string? Mood { get; set; }
    [Range(0, int.MaxValue)] public int DurationSeconds { get; set; }
}

public class PracticeSessionResponse
{
    public Guid SessionId { get; set; }
    public string Instrument { get; set; } = string.Empty;
    public string? DrillType { get; set; }
    public string? Mood { get; set; }
    public int DurationSeconds { get; set; }
    public DateTime CompletedAt { get; set; }
}

public class CreateJournalEntryRequest
{
    public string? AudioUrl { get; set; }
    public string? Note { get; set; }
}

public class JournalEntryResponse
{
    public Guid EntryId { get; set; }
    public string? AudioUrl { get; set; }
    public string? Note { get; set; }
    public DateTime RecordedAt { get; set; }
    public string SyncStatus { get; set; } = "Pending";
}

public class SaveLyricDraftRequest
{
    public string? Title { get; set; }
    [Required] public string Content { get; set; } = string.Empty;
}

public class LyricDraftResponse
{
    public Guid DraftId { get; set; }
    public string? Title { get; set; }
    public string Content { get; set; } = string.Empty;
    public DateTime LastEditedAt { get; set; }
}

public class CreatePerformancePostRequest
{
    [Required] public string AudioUrl { get; set; } = string.Empty;
    public Guid? ChallengeId { get; set; }
    public string? Caption { get; set; }
}

public class PerformancePostResponse
{
    public Guid PostId { get; set; }
    public string Author { get; set; } = string.Empty;
    public string AudioUrl { get; set; } = string.Empty;
    public string? Caption { get; set; }
    public int LikeCount { get; set; }
    public int CommentCount { get; set; }
    public DateTime PostedAt { get; set; }
}

public class CreateCommentRequest
{
    [Required, MaxLength(500)] public string Text { get; set; } = string.Empty;
}

public class CommentResponse
{
    public Guid CommentId { get; set; }
    public string Author { get; set; } = string.Empty;
    public string Text { get; set; } = string.Empty;
    public DateTime PostedAt { get; set; }
}
