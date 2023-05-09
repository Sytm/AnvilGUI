plugins { alias(libs.plugins.spotless) }

allprojects {
    repositories {
        mavenCentral()
        maven("https://repo.codemc.org/repository/nms/")
        maven("https://libraries.minecraft.net")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
    }
}

spotless {
    java {
        target("**/*.java")

        palantirJavaFormat()
        removeUnusedImports()
        importOrder()
    }
    kotlin {
        target("**/*.kts")

        ktfmt().dropboxStyle()
    }
}
