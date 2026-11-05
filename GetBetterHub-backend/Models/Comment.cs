using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace GetBetterHub.Api.Models;

public class Comment
{
    [Key]
    public Guid CommentId { get; set; } = Guid.NewGuid();

    [Required]
    public Guid PostId { get; set; }

    [ForeignKey(nameof(PostId))]
    public PerformancePost? Post { get; set; }

    [Required]
    public Guid UserId { get; set; }

    [ForeignKey(nameof(UserId))]
    public User? User { get; set; }

    [Required, MaxLength(500)]
    public string Text { get; set; } = string.Empty;

    public DateTime PostedAt { get; set; } = DateTime.UtcNow;
}
