# Get Better Hub (GBH)

A cross-discipline music practice app for Android - guided practice drills, mood-based coaching, lyric writing, an audio practice journal, and a community challenge arena, backed by a REST API.

Built for **OPSC6312 (Open Source Coding)** - Portfolio of Evidence, Part II.

**Demo video:** https://youtu.be/sl6WmZgiOac?si=gEWJmVy7EOq6vkgt

---

## What it does

| Screen | What it's for |
|---|---|
| **Login / Register** | Email + encrypted password, or Google SSO |
| **Home Dashboard** | Streak, level, XP, and quick access to every feature |
| **Practice Roulette** | Spins a random practice drill matched to instrument/level, with a live timer |
| **Mood Coach** | Pick how practice feels right now; get a tailored session suggestion |
| **Lyric Sketchpad** | Write and save lyric drafts, synced across devices |
| **Soundscape Journal** | Record real audio practice notes (mic recording + playback), synced to the server |
| **Challenge Arena** | Community feed, weekly challenges, leaderboard, and earned badges - post practice evidence pulled straight from your Journal |

## Tech stack

**Android app**
- Kotlin, Jetpack Compose (Material 3)
- Room — local, offline-first persistence
- Retrofit + OkHttp — REST API client
- Google Sign-In (OAuth 2.0 SSO)
- Native `MediaRecorder` / `MediaPlayer` for real audio capture and playback

**Backend API**
- ASP.NET Core Web API (.NET 8)
- Entity Framework Core + SQL Server (LocalDB for local dev, Azure SQL for production)
- JWT bearer authentication, BCrypt password hashing
- Google ID token verification for SSO
- Local file storage for uploaded audio (swappable for Azure Blob Storage)

## How it works: offline-first with sync

Every write (a completed practice session, a saved lyric draft, a journal recording) lands in the local Room database immediately, so the app never blocks on a network connection. Each item is tagged `synced = false` until it's successfully pushed to the API. A sync sweep runs automatically whenever you're signed in, retrying anything still pending - so going offline never loses data, it just waits for a connection.

## Project structure

```
.
├── GetBetterHub-android/     # Android app (Kotlin, Jetpack Compose)
│   └── app/src/main/java/com/getbetterhub/app/
│       ├── ui/screens/       # One Composable per screen
│       ├── ui/components/    # Shared UI building blocks
│       ├── ui/theme/         # Colors, typography
│       ├── data/local/       # Room database, DAOs, offline repository
│       ├── data/remote/      # Retrofit API, network repository, JWT token store
│       └── audio/            # MediaRecorder/MediaPlayer wrapper
│
└── GetBetterHub-backend/     # ASP.NET Core Web API
    └── GetBetterHub.Api/
        ├── Controllers/      # Auth, Users, PracticeSessions, JournalEntries, LyricDrafts, PerformancePosts, Uploads
        ├── Models/           # EF Core entities
        ├── DTOs/             # Request/response shapes
        ├── Data/             # DbContext
        └── Services/         # JWT issuing, current-user helpers
```

## Getting it running

### Backend

1. Install the [.NET 8 SDK](https://dotnet.microsoft.com/download) and SQL Server LocalDB (comes with Visual Studio's ".NET desktop development" workload).
2. In `GetBetterHub-backend/GetBetterHub.Api/appsettings.json`, set a real `Jwt:Key` (32+ random characters) and, if testing Google SSO, your `Google:ClientId`.
3. From that folder:
   ```
   dotnet tool install --global dotnet-ef   # once
   dotnet ef migrations add InitialCreate
   dotnet ef database update
   dotnet run
   ```
4. Swagger UI is available at `/swagger` for trying endpoints directly.

### Android app

1. Open `GetBetterHub-android/` in Android Studio via **File → Open** (not *New Project*) and let Gradle sync.
2. Point the app at your running backend in `NetworkModule.kt`:
   - Emulator → `http://10.0.2.2:<port>/`
   - Physical device → your machine's LAN IP, same Wi-Fi network
3. Run on an emulator or device (minSdk 26).

Full setup detail, including deploying the backend to Azure, is in `GetBetterHub-backend/GetBetterHub.Api/README.md`.

## Known limitations

- Multi-language resources (English/isiZulu/Afrikaans) exist but aren't yet wired into the screens - UI text is currently English-only.
- No dedicated Settings screen yet.
- No automated tests yet.
- Verified by careful review rather than a live build in this dev environment - first build may surface something minor.

## Author

Siyabonga Ndlovu - ST10443863
