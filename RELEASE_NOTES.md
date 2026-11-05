# Release notes

## Get Better Hub 1.0 (final PoE build)

Everything below is **new since the Part 2 prototype**.

### Innovative features
- **Achievements system.** 15 core achievements plus one for each instrument you practise, with rarity tiers (Common, Rare, Epic, Legendary), live progress and "remaining" counters. A notification is raised on unlock.
- **Practice Roulette by instrument and level.** 63 drills across 7 instruments and 3 levels. The roulette draws from the instruments ticked on your profile, with a live timer, percent of suggested time and per-exercise progress.
- **Mood-matched drills.** Mood Coach now chooses a drill that suits your mood and energy.
- **Profiles and followers.** Avatar (photo, colour or emoji), banner, @handle (unique), bio, privacy switches, a public profile view, user search, follow / unfollow and private-account follow requests.
- **Multi-language interface.** English, isiZulu, Afrikaans and isiXhosa, switched live from Settings.
- **Notifications.** Daily practice reminder (WorkManager, works offline), achievement alerts, follower alerts pulled from the API, a bell inbox with unread badge, and a test-notification button.
- **Visible offline mode.** Live Synced / Syncing / Offline pill, pending-change counter, "Sync now" and automatic catch-up when the phone reconnects.

### Improvements
- New Settings screen, and a shared top bar (profile, search, sync, bell, log out) on every screen.
- Every screen now scrolls, so nothing is cut off on small phones.
- Lyric editor delete button now works.
- API address is a build setting (`-PapiBaseUrl`) instead of a hard-coded constant.
- Fixed missing Compose imports that stopped `HubComponents.kt` from compiling.

### API changes
- `PUT/GET api/users/me` gained display name, handle (409 if taken), skill level, instruments, avatar and privacy fields, plus follower/following counts.
- New: `GET api/people/search`, `POST/DELETE api/follows/{id}`, `GET api/follows/requests`, `POST api/follows/requests/{id}/approve|decline`, `GET api/notifications`.

### Engineering
- Unit tests for the drill library, achievements and profile rules, run by GitHub Actions on every push.
- Signed-release support through `keystore.properties` (git-ignored).
- Logging through `AppLog` (tag `GBH`).
