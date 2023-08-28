plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    api("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:24.0.1")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
    withJavadocJar()
    withSourcesJar()
}

publishing {
    repositories {
        maven {
            name = "md5lukasReposilite"

            url =
                uri(
                    "https://repo.md5lukas.de/${
                        if (version.toString().endsWith("-SNAPSHOT")) {
                            "snapshots"
                        } else {
                            "releases"
                        }
                    }")

            credentials(PasswordCredentials::class)
            authentication { create<BasicAuthentication>("basic") }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifact(tasks.getByName("javadocJar"))
            artifact(tasks.getByName("sourcesJar"))
        }
    }
}
