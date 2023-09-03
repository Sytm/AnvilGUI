plugins {
    `java-library`
    alias(libs.plugins.shadow)
    alias(libs.plugins.runPaper)
}

dependencies {
    api("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
    implementation(project(":anvilgui"))
}

tasks {
    runServer { minecraftVersion("1.20.1") }
    shadowJar {
        archiveClassifier.set("")

        dependencies { include(project(":anvilgui")) }
    }
}

java { toolchain { languageVersion = JavaLanguageVersion.of(17) } }
