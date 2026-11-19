/*
 * Combined Reddit NSFW modes:
 * - Show mature content OFF: Block NSFW
 * - Show mature content ON: NSFW only
 *
 * Derivative work based on the GPLv3 Reddit NSFW Blocker and Reddit NSFW Only patches.
 * See NOTICE and LICENSE.
 */
package io.github.redditnsfwonly.patches

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import io.github.redditnsfwonly.patches.Constants.COMPATIBILITY_REDDIT
import java.util.logging.Logger

private const val EXTENSION_CLASS =
    "Lio/github/redditnsfwonly/extension/BlockNsfwContentPatch;"

private val hideIncognitoBlurTogglePatch = resourcePatch {
    execute {
        val layout = "res/layout/screen_leave_incognito_mode_modal.xml"
        if (!get(layout).exists()) {
            Logger.getLogger(this::class.java.name).warning(
                "Could not find $layout. Incognito blur toggle will remain visible."
            )
            return@execute
        }

        document(layout).use { document ->
            val views = document.getElementsByTagName("*")
            for (i in 0 until views.length) {
                val view = views.item(i) as? org.w3c.dom.Element ?: continue
                val id = view.getAttribute("android:id")
                // Keep Reddit's mature-content switch visible: it is the live mode selector.
                if (id.endsWith("/toggle_blur_nsfw")) {
                    view.setAttribute("android:visibility", "gone")
                }
            }
        }
    }
}

@Suppress("unused")
val nsfwOnlyPatch = bytecodePatch(
    name = "NSFW mode switch (Block / NSFW-only)",
    description = "Uses Reddit's Show mature content switch as a live mode selector: OFF blocks NSFW posts; " +
        "ON keeps only NSFW posts. Applies across modern Home, Compose feeds and legacy listings."
) {
    compatibleWith(COMPATIBILITY_REDDIT)
    extendWith("extensions/nsfwonly.mpe")
    dependsOn(hideIncognitoBlurTogglePatch)

    execute {
        val logger = Logger.getLogger(this::class.java.name)

        // Keep the actual Reddit account/local preference as the source of truth.
        // The existing app switch now selects the patch mode instead of being forced on/off.
        AccountPreferencesGetOver18Fingerprint.method.addInstructions(
            0,
            """
                iget-boolean p0, p0, $ACCOUNT_PREFERENCES_CLASS->over18:Z
                invoke-static { p0 }, $EXTENSION_CLASS->setNsfwOnlyMode(Z)V
                return p0
            """
        )
        AccountPreferencesGetSearchIncludeOver18Fingerprint.method.addInstructions(
            0,
            """
                invoke-static { }, $EXTENSION_CLASS->isNsfwOnlyMode()Z
                move-result p0
                return p0
            """
        )
        PreferenceRepositorySetOver18Fingerprint.method.addInstruction(
            0,
            "invoke-static { p1 }, $EXTENSION_CLASS->setNsfwOnlyMode(Z)V"
        )

        // Make both known blur decisions follow the selected mode.
        // The media-viewer renderer may have an additional path; that still needs on-device DEX verification.
        AccountPreferencesGetNoProfanityFingerprint.method.addInstructions(
            0,
            """
                invoke-static { }, $EXTENSION_CLASS->getBlurEnabled()Z
                move-result p0
                return p0
            """
        )
        PreferenceRepositoryIsBlurNsfwFingerprint.method.addInstructions(
            0,
            """
                invoke-static { }, $EXTENSION_CLASS->getBlurEnabled()Z
                move-result p0
                return p0
            """
        )

        // Safe search ON while blocking NSFW, OFF while filtering to NSFW-only.
        SafeSearchStoredValueFingerprint.matchOrNull()?.let {
            val safeSearchMode = """
                invoke-static { }, $EXTENSION_CLASS->isNsfwOnlyMode()Z
                move-result p0
                if-eqz p0, :block_nsfw
                sget-object p0, Lcom/reddit/domain/SafeSearch;->Off:Lcom/reddit/domain/SafeSearch;
                return-object p0
                :block_nsfw
                sget-object p0, Lcom/reddit/domain/SafeSearch;->On:Lcom/reddit/domain/SafeSearch;
                return-object p0
            """

            it.method.addInstructions(0, safeSearchMode)
            SafeSearchValueFingerprint.method.addInstructions(0, safeSearchMode)
            SafeSearchEnabledFingerprint.method.addInstructions(
                0,
                """
                    invoke-static { }, $EXTENSION_CLASS->isSafeSearchEnabled()Z
                    move-result p0
                    return p0
                """
            )
        } ?: logger.warning("Could not find the safe search repository")

        // Keep the mature-content option visible as the mode switch; hide only the competing blur option.
        settingsItemSessionsFingerprint("key_pref_blur_nsfw").matchOrNull()?.method?.addInstructions(
            0,
            """
                invoke-static { }, Ljava/util/Collections;->emptySet()Ljava/util/Set;
                move-result-object p0
                return-object p0
            """
        ) ?: logger.warning("Could not hide the 'Blur NSFW images' settings item")

        // Modern Home is GraphQL cell-backed. Filter its response before mapping, preserving retained edge objects.
        NsfwHomeFeedPageFingerprint.method.addInstructions(
            0,
            """
                invoke-static/range { p1 .. p1 }, $EXTENSION_CLASS->filterHomeFeedResponse(Ljava/lang/Object;)V
            """
        )

        // Other Compose feeds use FeedData; use the upstream blocker injection point.
        FeedDataConstructorFingerprint.matchOrNull()?.method?.addInstructions(
            0,
            """
                invoke-static { p1 }, $EXTENSION_CLASS->filterFeedItems(Ljava/util/List;)Ljava/util/List;
                move-result-object p1
            """
        ) ?: logger.warning("Could not find the Compose FeedData constructor; non-Home Compose filtering may be unavailable")

        // Legacy listings (subreddits, profiles and older endpoints).
        fun filterListing(fingerprint: Fingerprint) {
            fingerprint.method.apply {
                val index = fingerprint.instructionMatches.first().index
                val register = getInstruction<TwoRegisterInstruction>(index).registerA
                addInstructions(
                    index,
                    """
                        invoke-static { v$register }, $EXTENSION_CLASS->filterLinks(Ljava/util/List;)Ljava/util/List;
                        move-result-object v$register
                    """
                )
            }
        }
        filterListing(ListingFingerprint)
    }
}