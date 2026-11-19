/*
 * Copyright 2026 warleysr.
 * https://github.com/warleysr/reddit-nsfw-blocker
 *
 * Based on Morphe Patches, Copyright 2026 Morphe.
 * https://github.com/MorpheApp/morphe-patches
 *
 * See the included NOTICE file for GPLv3 Section 7 terms that apply to this code.
 */
package io.github.redditnsfwonly.patches

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import app.morphe.patcher.newInstance
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal const val ACCOUNT_PREFERENCES_CLASS = "Lcom/reddit/domain/model/AccountPreferences;"

internal object AccountPreferencesGetOver18Fingerprint : Fingerprint(
    definingClass = ACCOUNT_PREFERENCES_CLASS,
    name = "getOver18",
    returnType = "Z",
    parameters = listOf()
)

internal object AccountPreferencesGetSearchIncludeOver18Fingerprint : Fingerprint(
    definingClass = ACCOUNT_PREFERENCES_CLASS,
    name = "getSearchIncludeOver18",
    returnType = "Z",
    parameters = listOf()
)

/** AccountPreferences.getNoProfanity() supplies the persisted NSFW media-blur preference. */
internal object AccountPreferencesGetNoProfanityFingerprint : Fingerprint(
    definingClass = ACCOUNT_PREFERENCES_CLASS,
    name = "getNoProfanity",
    returnType = "Z",
    parameters = listOf()
)

/**
 * RedditPreferenceRepository.isOver18(): logged in reads account preferences; incognito reads
 * the local nsfw_over18_enabled preference. The getter/setter hooks update our runtime mode.
 */
internal object PreferenceRepositoryIsOver18Fingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf(),
    filters = listOf(
        methodCall(
            opcode = Opcode.INVOKE_INTERFACE,
            smali = "Lcom/reddit/session/Session;->isIncognito()Z"
        ),
        string("nsfw_over18_enabled"),
        methodCall(
            opcode = Opcode.INVOKE_VIRTUAL,
            smali = "$ACCOUNT_PREFERENCES_CLASS->getOver18()Z"
        )
    )
)

/** RedditPreferenceRepository.setOver18(Boolean), called by the native settings switch. */
internal object PreferenceRepositorySetOver18Fingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Z", "L"),
    filters = listOf(
        newInstance($$"Lcom/reddit/account/repository/RedditPreferenceRepository$setOver18$1;")
    )
)

private const val SAFE_SEARCH_CLASS = "Lcom/reddit/domain/SafeSearch;"

internal object SafeSearchStoredValueFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = SAFE_SEARCH_CLASS,
    parameters = listOf(),
    filters = listOf(string("com.reddit.search.repository.SAFE_SEARCH_ENUM"))
)

internal object SafeSearchValueFingerprint : Fingerprint(
    classFingerprint = SafeSearchStoredValueFingerprint,
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = SAFE_SEARCH_CLASS,
    parameters = listOf(),
    filters = listOf(string("nsfw_over18_enabled"))
)

internal object SafeSearchEnabledFingerprint : Fingerprint(
    classFingerprint = SafeSearchStoredValueFingerprint,
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf(),
    filters = listOf(string("com.reddit.search.repository.SAFE_SEARCH_ENABLED"))
)

/**
 * FeedData constructor used by Reddit's Compose feeds. The string fingerprint distinguishes
 * the data model from unrelated list-bearing constructors.
 */
private object FeedDataToStringFingerprint : Fingerprint(
    name = "toString",
    returnType = "Ljava/lang/String;",
    parameters = listOf(),
    filters = listOf(string("FeedData(items="))
)

internal object FeedDataConstructorFingerprint : Fingerprint(
    classFingerprint = FeedDataToStringFingerprint,
    name = "<init>",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR),
    returnType = "V",
    filters = listOf(
        methodCall(
            opcode = Opcode.INVOKE_DIRECT,
            smali = "Ljava/lang/Object;-><init>()V"
        )
    )
)

internal object NsfwHomeFeedPageFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("L", "Ljava/lang/Integer;", "L"),
    filters = listOf(
        fieldAccess(
            smali = "Lcom/reddit/feeds/data/FeedType;->HOME:Lcom/reddit/feeds/data/FeedType;"
        )
    )
)

/** Sessions where a Reddit settings item is shown, identified by its item key. */
internal fun settingsItemSessionsFingerprint(key: String) = Fingerprint(
    classFingerprint = Fingerprint(
        name = "<init>",
        returnType = "V",
        filters = listOf(string(key))
    ),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Ljava/util/Set;",
    parameters = listOf()
)

// Morphe's Hide Ads patch also modifies this constructor.
internal object ListingFingerprint : Fingerprint(
    definingClass = "Lcom/reddit/domain/model/listing/Listing;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR),
    filters = listOf(
        fieldAccess(
            opcode = Opcode.IPUT_OBJECT,
            smali = "Lcom/reddit/domain/model/listing/Listing;->children:Ljava/util/List;"
        ),
        fieldAccess(
            opcode = Opcode.IPUT_OBJECT,
            smali = "Lcom/reddit/domain/model/listing/Listing;->after:Ljava/lang/String;"
        ),
        fieldAccess(
            opcode = Opcode.IPUT_OBJECT,
            smali = "Lcom/reddit/domain/model/listing/Listing;->before:Ljava/lang/String;"
        )
    )
)

internal object PreferenceRepositoryIsBlurNsfwFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf(),
    filters = listOf(
        methodCall(
            opcode = Opcode.INVOKE_INTERFACE,
            smali = "Lcom/reddit/session/Session;->isIncognito()Z"
        ),
        string("nsfw_blur_enabled"),
        methodCall(
            opcode = Opcode.INVOKE_VIRTUAL,
            smali = "$ACCOUNT_PREFERENCES_CLASS->getNoProfanity()Z"
        )
    )
)
