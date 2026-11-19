# Changelog

## 0.3.0 — Combined live NSFW modes (development candidate)

- Combine the Block NSFW behavior from warleysr's Reddit NSFW Blocker with the NSFW-only filtering from Berkelyum9's Reddit NSFW Only.
- Reuse Reddit's native **Show mature content** setting as the live selector: OFF = Block NSFW; ON = NSFW-only.
- Persist the selection through Reddit's own account/local preference setter; remove the previous forced preference and asynchronous account-sync behavior.
- Apply mode-aware filtering to modern Home GraphQL edges, Compose FeedData, and legacy Listings.
- Preserve exact retained GraphQL edge objects, media payloads and non-post layout units.
- Make the known blur-preference and safe-search decisions mode-aware.
- Add regression coverage for both modes and live switching.
- Add Reddit 2026.40.0 to the candidate compatibility targets, pending exact APK fingerprint verification.

Verification boundary: unit tests and Morphe bundle compilation do not prove 2026.40.0 fingerprint matches or fix every image/video viewer blur path. Device validation remains required.
