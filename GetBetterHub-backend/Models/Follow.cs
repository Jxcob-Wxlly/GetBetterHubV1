using System.ComponentModel.DataAnnotations;

namespace GetBetterHub.Api.Models;

/// <summary>One user following another. Status is "accepted", or "pending" while a private account has not approved yet.</summary>
public class Follow
{
    public Guid FollowerId { get; set; }
    public User? Follower { get; set; }

    public Guid FolloweeId { get; set; }
    public User? Followee { get; set; }

    [Required, MaxLength(10)]
    public string Status { get; set; } = "accepted";

    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
}
