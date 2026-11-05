using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using GetBetterHub.Api.Services;

namespace GetBetterHub.Api.Controllers;

/// <summary>
/// Stores uploaded audio clips on local disk under wwwroot/uploads and returns a URL the
/// clip can be played back from. This stands in for Azure Blob Storage (named in the
/// Planning &amp; Design document) so the app is fully testable without an Azure account —
/// swap the body of Upload() for a BlobServiceClient call when you're ready to deploy for
/// real; the controller's request/response shape wouldn't need to change.
/// </summary>
[ApiController]
[Route("api/uploads")]
[Authorize]
public class UploadsController : ControllerBase
{
    private readonly IWebHostEnvironment _env;
    private static readonly HashSet<string> AllowedExtensions = new() { ".m4a", ".mp3", ".wav", ".aac" };
    private const long MaxFileSizeBytes = 15 * 1024 * 1024; // 15 MB — generous for a short practice/journal clip

    public UploadsController(IWebHostEnvironment env) => _env = env;

    [HttpPost("audio")]
    [RequestSizeLimit(MaxFileSizeBytes)]
    public async Task<IActionResult> UploadAudio(IFormFile file)
    {
        if (file.Length == 0)
            return BadRequest(new { message = "No file was sent." });

        if (file.Length > MaxFileSizeBytes)
            return BadRequest(new { message = "File is too large (15 MB max)." });

        var extension = Path.GetExtension(file.FileName).ToLowerInvariant();
        if (!AllowedExtensions.Contains(extension))
            return BadRequest(new { message = $"Unsupported file type '{extension}'." });

        var userId = User.GetUserId();
        var uploadsRoot = Path.Combine(_env.WebRootPath ?? Path.Combine(_env.ContentRootPath, "wwwroot"), "uploads", userId.ToString());
        Directory.CreateDirectory(uploadsRoot);

        var fileName = $"{Guid.NewGuid()}{extension}";
        var filePath = Path.Combine(uploadsRoot, fileName);

        using (var stream = new FileStream(filePath, FileMode.Create))
        {
            await file.CopyToAsync(stream);
        }

        var url = $"{Request.Scheme}://{Request.Host}/uploads/{userId}/{fileName}";
        return Ok(new { url });
    }
}
