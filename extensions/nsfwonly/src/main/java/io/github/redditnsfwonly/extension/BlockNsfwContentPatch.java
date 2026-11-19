/*
 * Reddit NSFW Only - combined live modes based on GPLv3 work from:
 * https://github.com/warleysr/reddit-nsfw-blocker
 * https://github.com/Berkelyum9/reddit-nsfw-only
 * See NOTICE and LICENSE for applicable terms.
 */
package io.github.redditnsfwonly.extension;

import android.util.Log;
import com.reddit.domain.model.Link;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Runtime policy for the combined NSFW mode patch.
 *
 * The Reddit "Show mature content" switch is the mode selector:
 * OFF = Block NSFW; ON = NSFW only.
 */
@SuppressWarnings({"unused", "rawtypes", "unchecked"})
public final class BlockNsfwContentPatch {
    private static final String TAG = "RedditNsfwModes";

    // Default matches Reddit's usual disabled mature-content setting.
    private static volatile boolean nsfwOnlyMode = false;
    private static volatile boolean modeChangedByUser = false;

    private BlockNsfwContentPatch() {}

    /** Sync initial account/incognito preference without overriding a live user choice. */
    public static void syncNsfwOnlyMode(boolean enabled) {
        if (modeChangedByUser) return;
        updateMode(enabled);
    }

    /** Called by Reddit's native preference setter when the user changes the mode switch. */
    public static void setNsfwOnlyMode(boolean enabled) {
        modeChangedByUser = true;
        updateMode(enabled);
    }

    private static void updateMode(boolean enabled) {
        boolean changed = nsfwOnlyMode != enabled;
        nsfwOnlyMode = enabled;
        if (changed) {
            Log.i(TAG, "Mode changed to " + (enabled ? "NSFW only" : "Block NSFW"));
        }
    }

    public static boolean isNsfwOnlyMode() {
        return nsfwOnlyMode;
    }

    /** Reddit's media preference is true only in Block NSFW mode. */
    public static boolean getBlurEnabled() {
        return !nsfwOnlyMode;
    }

    /** Safe search is enabled in Block NSFW mode and disabled in NSFW-only mode. */
    public static boolean isSafeSearchEnabled() {
        return !nsfwOnlyMode;
    }

    private static boolean shouldKeepPost(boolean nsfw) {
        return nsfwOnlyMode == nsfw;
    }

    /**
     * Legacy Reddit listing path. Keep non-Link structural units unchanged.
     */
    public static List<?> filterLinks(List<?> list) {
        if (list == null || list.isEmpty()) return list;

        try {
            List<Object> filtered = null;
            for (int i = 0; i < list.size(); i++) {
                Object item = list.get(i);
                if (item instanceof Link link && !shouldKeepPost(link.getOver18())) {
                    if (filtered == null) {
                        filtered = new ArrayList<>(list.subList(0, i));
                    }
                    Log.d(TAG, "Removing " + (link.getOver18() ? "NSFW" : "SFW") + " legacy link");
                } else if (filtered != null) {
                    filtered.add(item);
                }
            }
            return filtered == null ? list : filtered;
        } catch (Throwable ex) {
            Log.e(TAG, "filterLinks failed; leaving original listing intact", ex);
            return list;
        }
    }

    /**
     * Compose FeedData path used by feeds beyond modern Home.
     * Unknown or structural feed units are preserved; only positively classified posts are filtered.
     */
    public static List<?> filterFeedItems(List<?> list) {
        if (list == null || list.isEmpty()) return list;

        try {
            List<Object> filtered = null;
            for (int i = 0; i < list.size(); i++) {
                Object item = list.get(i);
                Boolean nsfw = null;

                if (item instanceof Link link) {
                    nsfw = link.getOver18();
                } else {
                    NsfwCellScanner.Scan scan = NsfwCellScanner.scan(item);
                    if (scan != null) nsfw = scan.nsfw;
                }

                if (nsfw != null && !shouldKeepPost(nsfw)) {
                    if (filtered == null) {
                        filtered = new ArrayList<>(list.subList(0, i));
                    }
                    Log.d(TAG, "Removing " + (nsfw ? "NSFW" : "SFW") + " compose feed item");
                } else if (filtered != null) {
                    filtered.add(item);
                }
            }
            return filtered == null ? list : filtered;
        } catch (Throwable ex) {
            Log.e(TAG, "filterFeedItems failed; leaving original feed intact", ex);
            return list;
        }
    }

    /**
     * Modern Home path. Reddit's GraphQL edges are filtered before mapping so retained
     * NSFW/SFW items keep their original title, preview, image/video and renderer payload.
     * Unknown response shapes and non-post units are preserved.
     */
    public static void filterHomeFeedResponse(Object response) {
        if (response == null) return;

        try {
            for (Field field : response.getClass().getDeclaredFields()) {
                if (!List.class.isAssignableFrom(field.getType())) continue;
                field.setAccessible(true);
                Object value = field.get(response);
                if (!(value instanceof List<?> raw) || raw.isEmpty()) continue;

                List<Object> edges = (List<Object>) raw;
                List<Object> keep = new ArrayList<>(edges.size());
                int recognizedPosts = 0;
                int removedPosts = 0;

                for (Object edge : edges) {
                    NsfwCellScanner.Scan scan = NsfwCellScanner.scan(edge);
                    if (scan == null) {
                        keep.add(edge);
                        continue;
                    }

                    recognizedPosts++;
                    if (shouldKeepPost(scan.nsfw)) {
                        keep.add(edge);
                    } else {
                        removedPosts++;
                        Log.d(TAG, "Removing " + (scan.nsfw ? "NSFW" : "SFW") + " Home post");
                    }
                }

                if (recognizedPosts == 0) {
                    continue;
                }

                if (keep.size() == edges.size()) {
                    Log.d(TAG, "Home feed unchanged: no posts matched the opposite mode");
                    return;
                }

                // Preserve exact edge objects and media payloads; mutate only after scanning succeeds.
                edges.clear();
                edges.addAll(keep);
                Log.d(TAG, "Home filter kept " + keep.size() + " feed units and removed "
                        + removedPosts + " posts");
                return;
            }
            Log.d(TAG, "Home feed unchanged: no recognized post list found");
        } catch (Throwable ex) {
            Log.e(TAG, "filterHomeFeedResponse failed; leaving original feed intact", ex);
        }
    }
}