using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace GetBetterHub.Api.Models;

public class PerformancePost
{
    [Key]
    public Guid PostId { get; set; } = Guid.NewGuid();

    [Required]
    public Guid UserId { get; set; }

    [ForeignKey(nameof(UserId))]
    public User? User { get; set; }

    [Required]
    public string AudioUrl { get; set; } = string.Empty;

    // Links this post to a specific weekly challenge; null for a regular, non-challenge post.
    public Guid? ChallengeId { get; set; }

    [MaxLength(200)]
    public string? Caption { get; set; }

    public int LikeCount { get; set; } = 0;

    public DateTime PostedAt { get; set; } = DateTime.UtcNow;

    public List<Comment> Comments { get; set; } = new();
}
