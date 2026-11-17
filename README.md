# Reddit NSFW Only

An independently maintained Morphe patch fork by **cimon-bkr**.

## Current candidate

- Version: **0.2.1**
- Intended Reddit target inherited from upstream: **2026.39.0**
- Package: `com.reddit.frontpage`
- Goal: keep NSFW-only filtering while preserving normal Home-feed rendering.

### 0.2.1 changes

- Keep original GraphQL edge objects for NSFW posts so Reddit retains their complete title, thumbnail, image/video, flair, author and renderer metadata.
- Preserve non-post feed units such as layout/furniture entries.
- Remove only posts positively identified as SFW.
- Leave unknown response shapes intact instead of clearing the feed.
- Leave the original feed untouched if reflection/scanning fails.

**Verification status:** source changes are committed, but the GitHub Actions build and on-device Morphe verification are pending. Do not treat this candidate as a stable release until both pass.

## Install

A release asset will be linked here after CI succeeds. For now, use the repository's Actions build artifact only for testing.

## Origin and license

This is a modified derivative of GPLv3 projects:

- https://github.com/Berkelyum9/reddit-nsfw-only
- https://github.com/warleysr/reddit-nsfw-blocker
- https://github.com/MorpheApp/morphe-patches
- https://github.com/variablenine/morphe-patches

See `LICENSE` and `NOTICE`. This fork is not affiliated with Reddit or the upstream maintainers.


<!-- CI trigger: validate the 0.2.1 candidate; this commit does not publish a release. -->


<!-- v0.2.1 release trigger: CI tests and Morphe bundle build passed. -->


## v0.2.2 release candidate

This release disables the persisted account NSFW-blur preference and the repository blur decision. CI confirms the Java regression tests pass and the Morphe bundle compiles with a DEX payload for the declared Reddit 2026.39.0 target. The build does not execute the patch against an installed Reddit APK or independently prove that every media-viewer renderer honors these preferences; verify feed thumbnails and full-screen media after installing.
