# Changelog

## 0.2.2 — NSFW blur preference override

- Add a direct hook for `AccountPreferences.getNoProfanity()` to force the persisted account blur preference off.
- Override `RedditPreferenceRepository.isBlurNsfw()` to return false for logged-in and incognito preference decisions.
- Make both blur hooks mandatory patch fingerprints rather than silently warning when the repository-level hook is absent.
- Retain the 0.2.1 Home-feed integrity behavior and NSFW-only filtering.

Verification: this candidate must pass the Morphe build against the declared Reddit 2026.39.0 target and be tested in the feed and full-screen media viewer. A successful build alone does not prove the media viewer uses these same preference paths.
