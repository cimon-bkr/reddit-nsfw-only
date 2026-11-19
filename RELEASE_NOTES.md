# Reddit NSFW Modes v0.3.0-rc.1 — prerelease for testing

This candidate combines two modes behind Reddit's native **Show mature content** setting:

- **OFF — Block NSFW:** retain SFW posts, remove NSFW posts, enable blur and safe search.
- **ON — NSFW-only:** retain NSFW posts, remove SFW posts, disable the known blur-preference decisions and safe search.

Filtering is applied in the modern Home GraphQL path, the Compose FeedData path (when its fingerprint matches), and legacy Listings. Retained Home edge objects are not rebuilt, so their media and renderer payloads remain intact.

This prerelease is for testing only and is not a stable release. Reddit 2026.40.0 is in the official Morphe compatibility target list, but the custom fingerprints in this fork have not yet been matched against your exact installed APKM split set. The video-thumbnail and full-screen viewer blur paths have not been verified. Do not assume the blur fix is complete. Test the toggle and both media paths before relying on it.
