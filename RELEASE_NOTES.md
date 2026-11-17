# Reddit NSFW Only v0.2.1

This candidate fixes Home-feed rendering regressions while keeping NSFW-only filtering.

- Original GraphQL edge objects for retained NSFW posts are preserved unchanged, including the data Reddit uses to render titles, previews, images and videos.
- Non-post layout/furniture entries are preserved.
- Only positively classified SFW posts are removed.
- Unknown response shapes and scanner failures no longer clear feed lists.

This release is pending CI and real-device verification. Do not install as a stable release until those checks pass.
