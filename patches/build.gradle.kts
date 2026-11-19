group = "io.github.redditnsfwonly"

patches {
    about {
        name = "Reddit NSFW Modes"
        description = "Live switch between blocking NSFW posts and showing NSFW-only feeds"
        source = "https://github.com/cimon-bkr/reddit-nsfw-only"
        author = "Berkelyum9"
        contact = "https://github.com/cimon-bkr/reddit-nsfw-only/issues"
        website = "https://github.com/cimon-bkr/reddit-nsfw-only"
        license = "GNU General Public License v3.0, with preserved upstream NOTICE requirements"
    }
}

dependencies {
    implementation(libs.guava)
    implementation(libs.morphe.patches.library)
}
