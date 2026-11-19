# Reddit NSFW Modes v0.3.0 — development candidate

This candidate combines two modes behind Reddit's native **Show mature content** setting:

- **OFF — Block NSFW:** retain SFW posts, remove NSFW posts, enable blur and safe search.
- **ON — NSFW-only:** retain NSFW posts, remove SFW posts, disable the known blur-preference decisions and safe search.

Filtering is applied in the modern Home GraphQL path, the Compose FeedData path (when its fingerprint matches), and legacy Listings. Retained Home edge objects are not rebuilt, so their media and renderer payloads remain intact.

This is not a stable release. Exact 2026.40.0 DEX fingerprints and separate video-thumbnail / full-screen viewer blur paths still require validation against the installed APK and on-device testing.
