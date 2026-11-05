using System.ComponentModel.DataAnnotations;

namespace GetBetterHub.Api.Models;

/// <summary>An event for <see cref="UserId"/> caused by <see cref="ActorId"/>. The phone pulls these and shows them as notifications.</summary>
public class AppNotification
{
    [Key]
    public Guid NotificationId { get; set; } = Guid.NewGuid();

    public Guid UserId { get; set; }
    public User? User { get; set; }

    public Guid ActorId { get; set; }
    public User? Actor { get; set; }

    // new_follower | follow_request | request_approved
    [Required, MaxLength(30)]
    public string Type { get; set; } = string.Empty;

    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
}
