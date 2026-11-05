using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace GetBetterHub.Api.Models;

public class PracticeSession
{
    [Key]
    public Guid SessionId { get; set; } = Guid.NewGuid();

    [Required]
    public Guid UserId { get; set; }

    [ForeignKey(nameof(UserId))]
    public User? User { get; set; }

    [Required, MaxLength(50)]
    public string Instrument { get; set; } = string.Empty;

    // Drill type as selected by Practice Roulette, e.g. "scale", "riff", "vocal-warmup"
    [MaxLength(50)]
    public string? DrillType { get; set; }

    // Mood selected via Mood-Based Coach: flat | fired-up | foggy | frustrated
    [MaxLength(20)]
    public string? Mood { get; set; }

    public int DurationSeconds { get; set; }

    public DateTime CompletedAt { get; set; } = DateTime.UtcNow;
}
