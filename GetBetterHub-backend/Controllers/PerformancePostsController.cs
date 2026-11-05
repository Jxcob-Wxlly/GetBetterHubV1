using GetBetterHub.Api.Data;
using GetBetterHub.Api.DTOs;
using GetBetterHub.Api.Models;
using GetBetterHub.Api.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace GetBetterHub.Api.Controllers;

[ApiController]
[Route("api/performance-posts")]
[Authorize]
public class PerformancePostsController : ControllerBase
{
    private readonly AppDbContext _db;
    public PerformancePostsController(AppDbContext db) => _db = db;

    [HttpGet]
    public async Task<ActionResult<List<PerformancePostResponse>>> GetAll()
    {
        var posts = await _db.PerformancePosts
            .Include(p => p.User)
            .Include(p => p.Comments)
            .OrderByDescending(p => p.PostedAt)
            .Take(50)
            .Select(p => new PerformancePostResponse
            {
                PostId = p.PostId,
                Author = p.User!.Username,
                AudioUrl = p.AudioUrl,
                Caption = p.Caption,
                LikeCount = p.LikeCount,
                CommentCount = p.Comments.Count,
                PostedAt = p.PostedAt
            })
            .ToListAsync();

        return Ok(posts);
    }

    [HttpPost]
    public async Task<ActionResult<PerformancePostResponse>> Create(CreatePerformancePostRequest request)
    {
        var userId = User.GetUserId();
        var user = await _db.Users.FindAsync(userId);
        if (user == null) return Unauthorized();

        var post = new PerformancePost
        {
            UserId = userId,
            AudioUrl = request.AudioUrl,
            ChallengeId = request.ChallengeId,
            Caption = request.Caption
        };
        _db.PerformancePosts.Add(post);
        await _db.SaveChangesAsync();

        return Ok(new PerformancePostResponse
        {
            PostId = post.PostId,
            Author = user.Username,
            AudioUrl = post.AudioUrl,
            Caption = post.Caption,
            LikeCount = 0,
            CommentCount = 0,
            PostedAt = post.PostedAt
        });
    }

    [HttpPost("{id}/like")]
    public async Task<IActionResult> Like(Guid id)
    {
        var post = await _db.PerformancePosts.FindAsync(id);
        if (post == null) return NotFound();
        // Simple increment rather than a Likes join table — good enough for a v1 "like count",
        // at the cost of not being able to un-like or show who liked a post.
        post.LikeCount += 1;
        await _db.SaveChangesAsync();
        return Ok(new { likeCount = post.LikeCount });
    }

    [HttpGet("{id}/comments")]
    public async Task<ActionResult<List<CommentResponse>>> GetComments(Guid id)
    {
        var comments = await _db.Comments
            .Include(c => c.User)
            .Where(c => c.PostId == id)
            .OrderBy(c => c.PostedAt)
            .Select(c => new CommentResponse
            {
                CommentId = c.CommentId,
                Author = c.User!.Username,
                Text = c.Text,
                PostedAt = c.PostedAt
            })
            .ToListAsync();

        return Ok(comments);
    }

    [HttpPost("{id}/comments")]
    public async Task<ActionResult<CommentResponse>> AddComment(Guid id, CreateCommentRequest request)
    {
        var userId = User.GetUserId();
        var user = await _db.Users.FindAsync(userId);
        var post = await _db.PerformancePosts.FindAsync(id);
        if (user == null || post == null) return NotFound();

        var comment = new Comment { PostId = id, UserId = userId, Text = request.Text };
        _db.Comments.Add(comment);
        await _db.SaveChangesAsync();

        return Ok(new CommentResponse
        {
            CommentId = comment.CommentId,
            Author = user.Username,
            Text = comment.Text,
            PostedAt = comment.PostedAt
        });
    }
}
