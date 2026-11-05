using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace GetBetterHub.Api.Models;

public class LyricDraft
{
    [Key]
    public Guid DraftId { get; set; } = Guid.NewGuid();

    [Required]
    public Guid UserId { get; set; }

    [ForeignKey(nameof(UserId))]
    public User? User { get; set; }

    [MaxLength(100)]
    public string? Title { get; set; }

    public string Content { get; set; } = string.Empty;

    public DateTime LastEditedAt { get; set; } = DateTime.UtcNow;
}
