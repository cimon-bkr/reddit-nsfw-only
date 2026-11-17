# Changelog

## 0.2.1 — Home feed integrity and media preservation

- Retain original Reddit GraphQL edge objects for NSFW posts, preserving all renderer data including titles, thumbnails, image/video payloads, flair and author metadata.
- Preserve non-post feed units such as layout/furniture entries.
- Remove only posts positively identified as SFW by the existing GraphQL post-indicator scanner.
- Fail open on unknown response shapes; do not empty a feed when no post IDs can be read.
- Leave the original response untouched if reflection or scanning fails; remove the destructive list-clearing fallback.
- Add regression tests for mixed feeds, structural feed units, unknown response shapes, and retained media payload identity.

Verification: CI build and on-device Morphe testing are required before declaring this release stable.
