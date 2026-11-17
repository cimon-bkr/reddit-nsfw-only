package io.github.redditnsfwonly.extension;

import com.reddit.domain.model.Link;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class BlockNsfwContentPatchTest {
    enum CellIndicatorType { APP, NSFW, ORIGINAL, QUARANTINED, SPOILER }

    static final class Edge {
        Object id;
        Object indicators;
        final Object mediaPayload;
        Edge(String id, Object indicators) {
            this.id = id;
            this.indicators = indicators;
            this.mediaPayload = new Object();
        }
    }

    static final class HomeResponse {
        List<Object> edges;
        HomeResponse(Object... edges) { this.edges = new ArrayList<>(Arrays.asList(edges)); }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        Link nsfw = new Link(true);
        Link sfw = new Link(false);
        Object structural = new Object();
        List<?> legacy = BlockNsfwContentPatch.filterLinks(Arrays.asList(nsfw, sfw, structural));
        require(legacy.contains(nsfw), "legacy: NSFW link must survive");
        require(!legacy.contains(sfw), "legacy: SFW link must be removed");
        require(legacy.contains(structural), "legacy: non-Link structural item must survive");

        Edge nsfwEdge = new Edge("t3_nsfw", List.of(CellIndicatorType.NSFW));
        Edge sfwEdge = new Edge("t3_sfw", List.of(CellIndicatorType.SPOILER));
        Object nonPost = new Object();

        HomeResponse mixed = new HomeResponse(sfwEdge, nonPost, nsfwEdge);
        BlockNsfwContentPatch.filterHomeFeedResponse(mixed);
        require(mixed.edges.size() == 2 && mixed.edges.get(0) == nonPost
                        && mixed.edges.get(1) == nsfwEdge,
                "home: preserve non-post layout unit and confirmed NSFW edge");
        require(mixed.edges.get(1) == nsfwEdge && ((Edge) mixed.edges.get(1)).mediaPayload == nsfwEdge.mediaPayload,
                "home: retained edge and its media payload identity must be unchanged");

        HomeResponse allSfw = new HomeResponse(
                new Edge("t3_one", List.of(CellIndicatorType.SPOILER)),
                new Edge("t3_two", List.of(CellIndicatorType.ORIGINAL)));
        BlockNsfwContentPatch.filterHomeFeedResponse(allSfw);
        require(allSfw.edges.isEmpty(), "home: positively identified SFW posts are removed");

        Object unknownOne = new Object();
        Object unknownTwo = new Object();
        HomeResponse unknownShape = new HomeResponse(unknownOne, unknownTwo);
        BlockNsfwContentPatch.filterHomeFeedResponse(unknownShape);
        require(unknownShape.edges.size() == 2
                        && unknownShape.edges.get(0) == unknownOne
                        && unknownShape.edges.get(1) == unknownTwo,
                "home: unknown response shape must remain intact");

        System.out.println("BlockNsfwContentPatchTest: PASS");
    }
}
