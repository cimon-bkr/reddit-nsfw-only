# Reddit NSFW Modes

An independently maintained Morphe patch fork by **cimon-bkr**.

## v0.3.0 candidate — live mode selector

This branch combines the GPLv3-based features of:

- [Reddit NSFW Blocker](https://github.com/warleysr/reddit-nsfw-blocker)
- [Reddit NSFW Only](https://github.com/Berkelyum9/reddit-nsfw-only)

It uses Reddit's existing **Show mature content** switch as the live mode selector:

| Reddit setting | Patch mode | Feed behavior | Blur / safe search |
|---|---|---|---|
| OFF | **Block NSFW** | Keep SFW posts; remove NSFW posts | Blur ON; safe search ON |
| ON | **NSFW-only** | Keep NSFW posts; remove SFW posts | Blur OFF; safe search OFF |

The setting is persisted by Reddit itself. Switching it updates the patch's runtime mode without reinstalling or re-patching. A feed that is already on screen may need a refresh before its contents reflect the new mode.

### Coverage in this candidate

- Modern GraphQL Home feed, filtered before Reddit maps the feed cells.
- Compose FeedData feeds, when the upstream constructor fingerprint matches.
- Legacy Listing responses.
- Original retained GraphQL edge objects are preserved, including their image/video payloads and layout metadata.
- Unknown post shapes and non-post feed units are preserved; scanner failures fail open rather than blanking a feed.
- The competing **Blur NSFW images** setting is hidden so it cannot conflict with mode behavior. **Show mature content** remains visible as the mode selector.

### Important verification status

This is a development candidate, not a verified release. The branch declares Reddit 2026.40.0 as a target, matching the installed version reported during development, but custom method fingerprints still need validation against the exact installed APK/split APK set.

The two known preference hooks dynamically set the blur decision. This does **not yet prove** all thumbnails and full-screen media use those hooks. Reddit may have separate thumbnail/image-viewer rendering paths; those need inspection of the installed DEX and on-device testing before claiming blur is fixed.

CI tests and bundle compilation are not substitutes for applying the patch to Reddit and checking both modes in the feed, video thumbnails, and full-screen viewer.

## Install status

Do not use this development branch as a stable release yet. The existing stable fork release is [v0.2.2](https://github.com/cimon-bkr/reddit-nsfw-only/releases/tag/v0.2.2). The v0.3.0 bundle will be published only after compatibility and runtime checks.

## Build

The GitHub Actions workflow tests both mode policies and builds the Morphe bundle on branch pushes and pull requests. It publishes releases only from main when explicitly triggered.

## Origin and license

This fork is derivative work based on GPLv3 projects:

- https://github.com/warleysr/reddit-nsfw-blocker
- https://github.com/Berkelyum9/reddit-nsfw-only
- https://github.com/MorpheApp/morphe-patches
- https://github.com/variablenine/morphe-patches

See LICENSE and NOTICE for applicable GPLv3 terms. This project is not affiliated with Reddit or the upstream maintainers.
