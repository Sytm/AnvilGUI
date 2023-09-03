plugins { alias(libs.plugins.spotless) }

allprojects {
    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://libraries.minecraft.net")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
    }
}

spotless {
    java {
        target("**/*.java")

        palantirJavaFormat()
        removeUnusedImports()
        formatAnnotations()
        importOrder()
    }
    kotlin {
        target("**/*.kts")

        ktfmt().dropboxStyle()
    }
}
