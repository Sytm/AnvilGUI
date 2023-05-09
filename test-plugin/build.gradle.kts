plugins {
    `java-library`
    alias(libs.plugins.shadow)
    alias(libs.plugins.runPaper)
}

dependencies {
    api("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")
    implementation(project(":anvilgui", "shadow"))
}

tasks {
    runServer {
        minecraftVersion("1.20.1")
    }
    shadowJar {
        archiveClassifier.set("")

        dependencies {
            include(project(":anvilgui"))
        }
    }
}

java {
    targetCompatibility = JavaVersion.VERSION_17
    sourceCompatibility = JavaVersion.VERSION_17
}

