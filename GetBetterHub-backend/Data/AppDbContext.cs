using Microsoft.EntityFrameworkCore;
using GetBetterHub.Api.Models;

namespace GetBetterHub.Api.Data;

public class AppDbContext : DbContext
{
    public AppDbContext(DbContextOptions<AppDbContext> options) : base(options) { }

    public DbSet<User> Users => Set<User>();
    public DbSet<PracticeSession> PracticeSessions => Set<PracticeSession>();
    public DbSet<JournalEntry> JournalEntries => Set<JournalEntry>();
    public DbSet<LyricDraft> LyricDrafts => Set<LyricDraft>();
    public DbSet<PerformancePost> PerformancePosts => Set<PerformancePost>();
    public DbSet<Comment> Comments => Set<Comment>();
    public DbSet<Follow> Follows => Set<Follow>();
    public DbSet<AppNotification> Notifications => Set<AppNotification>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);

        modelBuilder.Entity<User>()
            .HasIndex(u => u.Email)
            .IsUnique();

        modelBuilder.Entity<User>()
            .HasIndex(u => u.Username)
            .IsUnique();

        // @handle is optional but unique when present (filtered index so many NULLs are allowed).
        modelBuilder.Entity<User>()
            .HasIndex(u => u.Handle)
            .IsUnique()
            .HasFilter("[Handle] IS NOT NULL");

        modelBuilder.Entity<Follow>().HasKey(f => new { f.FollowerId, f.FolloweeId });
        modelBuilder.Entity<Follow>()
            .HasOne(f => f.Follower).WithMany().HasForeignKey(f => f.FollowerId)
            .OnDelete(DeleteBehavior.Restrict); // two FKs to Users: avoid multiple cascade paths
        modelBuilder.Entity<Follow>()
            .HasOne(f => f.Followee).WithMany().HasForeignKey(f => f.FolloweeId)
            .OnDelete(DeleteBehavior.Restrict);

        modelBuilder.Entity<AppNotification>()
            .HasOne(n => n.User).WithMany().HasForeignKey(n => n.UserId)
            .OnDelete(DeleteBehavior.Cascade);
        modelBuilder.Entity<AppNotification>()
            .HasOne(n => n.Actor).WithMany().HasForeignKey(n => n.ActorId)
            .OnDelete(DeleteBehavior.Restrict);

        modelBuilder.Entity<PracticeSession>()
            .HasOne(p => p.User)
            .WithMany(u => u.PracticeSessions)
            .HasForeignKey(p => p.UserId)
            .OnDelete(DeleteBehavior.Cascade);

        modelBuilder.Entity<JournalEntry>()
            .HasOne(j => j.User)
            .WithMany(u => u.JournalEntries)
            .HasForeignKey(j => j.UserId)
            .OnDelete(DeleteBehavior.Cascade);

        modelBuilder.Entity<LyricDraft>()
            .HasOne(l => l.User)
            .WithMany(u => u.LyricDrafts)
            .HasForeignKey(l => l.UserId)
            .OnDelete(DeleteBehavior.Cascade);

        modelBuilder.Entity<PerformancePost>()
            .HasOne(p => p.User)
            .WithMany(u => u.PerformancePosts)
            .HasForeignKey(p => p.UserId)
            .OnDelete(DeleteBehavior.Cascade);

        modelBuilder.Entity<Comment>()
            .HasOne(c => c.Post)
            .WithMany(p => p.Comments)
            .HasForeignKey(c => c.PostId)
            .OnDelete(DeleteBehavior.Cascade);

        modelBuilder.Entity<Comment>()
            .HasOne(c => c.User)
            .WithMany(u => u.Comments)
            .HasForeignKey(c => c.UserId)
            .OnDelete(DeleteBehavior.Restrict); // avoid multiple cascade paths
    }
}
