package io.github.redditnsfwonly.extension;

import com.reddit.domain.model.Link;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class BlockNsfwContentPatchTest {
    enum CellIndicatorType { APP, NSFW, ORIGINAL, QUARANTINED, SPOILER }

    static final class Edge {
        final String id;
        final Object indicators;
        final Object mediaPayload;
        Edge(String id, Object indicators) {
            this.id = id;
            this.indicators = indicators;
            this.mediaPayload = new Object();
        }
    }

    static final class HomeResponse {
        final List<Object> edges;
        HomeResponse(Object... edges) {
            this.edges = new ArrayList<>(Arrays.asList(edges));
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static boolean containsIdentity(List<?> values, Object expected) {
        for (Object value : values) if (value == expected) return true;
        return false;
    }

    public static void main(String[] args) {
        Link nsfw = new Link(true);
        Link sfw = new Link(false);
        Object structural = new Object();

        // Default mode: Block NSFW. SFW posts and non-post structure survive.
        require(!BlockNsfwContentPatch.isNsfwOnlyMode(), "default mode is Block NSFW");
        require(BlockNsfwContentPatch.getBlurEnabled(), "Block NSFW keeps media blur enabled");
        require(BlockNsfwContentPatch.isSafeSearchEnabled(), "Block NSFW keeps safe search enabled");

        List<?> blockLinks = BlockNsfwContentPatch.filterLinks(Arrays.asList(nsfw, sfw, structural));
        require(!containsIdentity(blockLinks, nsfw), "Block NSFW removes legacy NSFW links");
        require(containsIdentity(blockLinks, sfw), "Block NSFW keeps legacy SFW links");
        require(containsIdentity(blockLinks, structural), "Block NSFW preserves structural units");

        Edge nsfwEdge = new Edge("t3_nsfw", List.of(CellIndicatorType.NSFW));
        Edge sfwEdge = new Edge("t3_sfw", List.of(CellIndicatorType.SPOILER));
        HomeResponse blockHome = new HomeResponse(nsfwEdge, structural, sfwEdge);
        BlockNsfwContentPatch.filterHomeFeedResponse(blockHome);
        require(blockHome.edges.size() == 2
                        && blockHome.edges.get(0) == structural
                        && blockHome.edges.get(1) == sfwEdge,
                "Block NSFW home mode keeps SFW and non-post units only");

        List<?> blockFeed = BlockNsfwContentPatch.filterFeedItems(Arrays.asList(nsfw, sfw, structural));
        require(!containsIdentity(blockFeed, nsfw) && containsIdentity(blockFeed, sfw)
                        && containsIdentity(blockFeed, structural),
                "Block NSFW filters Compose FeedData items");

        // Live switch: this is the same setting Reddit's native Show mature content switch writes.
        BlockNsfwContentPatch.setNsfwOnlyMode(true);
        require(BlockNsfwContentPatch.isNsfwOnlyMode(), "mode changes immediately to NSFW-only");
        require(!BlockNsfwContentPatch.getBlurEnabled(), "NSFW-only disables the blur preference");
        require(!BlockNsfwContentPatch.isSafeSearchEnabled(), "NSFW-only disables safe search");

        List<?> onlyLinks = BlockNsfwContentPatch.filterLinks(Arrays.asList(nsfw, sfw, structural));
        require(containsIdentity(onlyLinks, nsfw), "NSFW-only keeps legacy NSFW links");
        require(!containsIdentity(onlyLinks, sfw), "NSFW-only removes legacy SFW links");
        require(containsIdentity(onlyLinks, structural), "NSFW-only preserves structural units");

        HomeResponse onlyHome = new HomeResponse(sfwEdge, structural, nsfwEdge);
        BlockNsfwContentPatch.filterHomeFeedResponse(onlyHome);
        require(onlyHome.edges.size() == 2
                        && onlyHome.edges.get(0) == structural
                        && onlyHome.edges.get(1) == nsfwEdge,
                "NSFW-only home mode keeps NSFW and non-post units only");
        require(((Edge) onlyHome.edges.get(1)).mediaPayload == nsfwEdge.mediaPayload,
                "NSFW-only preserves the original edge and its media payload identity");

        List<?> onlyFeed = BlockNsfwContentPatch.filterFeedItems(Arrays.asList(nsfw, sfw, structural));
        require(containsIdentity(onlyFeed, nsfw) && !containsIdentity(onlyFeed, sfw)
                        && containsIdentity(onlyFeed, structural),
                "NSFW-only filters Compose FeedData items");

        // Unknown response shapes fail open in either mode; no blank-feed regression.
        Object unknownOne = new Object();
        Object unknownTwo = new Object();
        HomeResponse unknownShape = new HomeResponse(unknownOne, unknownTwo);
        BlockNsfwContentPatch.filterHomeFeedResponse(unknownShape);
        require(unknownShape.edges.size() == 2
                        && unknownShape.edges.get(0) == unknownOne
                        && unknownShape.edges.get(1) == unknownTwo,
                "unknown home response shapes remain intact");

        BlockNsfwContentPatch.setNsfwOnlyMode(false);
        require(!BlockNsfwContentPatch.isNsfwOnlyMode(), "mode can switch back without app restart");
        require(BlockNsfwContentPatch.getBlurEnabled(), "blur re-enables after switching back");
        require(BlockNsfwContentPatch.isSafeSearchEnabled(), "safe search re-enables after switching back");

        System.out.println("BlockNsfwContentPatchTest: PASS");
    }
}
