import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

plugins {
    `java-library`
    alias(libs.plugins.shadow)
    `maven-publish`
}

val nmsModules =
    rootDir
        .toPath()
        .listDirectoryEntries("1_*R?")
        .map { ":${it.name}" }
        .groupBy { it.substringAfter('_').substringBefore('_').toInt() }
        .toSortedMap()

dependencies {
    api("org.spigotmc:spigot-api:1.8-R0.1-SNAPSHOT")
    implementation(project(":abstraction"))
    nmsModules
        .flatMap { it.value }
        .forEach {
            runtimeOnly(project(it)) {
                isTransitive = false
                attributes {
                    // Necessary hack to convince gradle that the final Jar runs on Java 17, even
                    // though we only compile against Java 8
                    attribute(Attribute.of("org.gradle.jvm.version", Int::class.javaObjectType), 17)
                }
            }
        }
}

java {
    targetCompatibility = JavaVersion.VERSION_1_8
    sourceCompatibility = JavaVersion.VERSION_1_8
    withJavadocJar()
    withSourcesJar()
}

tasks.getByName<ShadowJar>("shadowJar") {
    archiveClassifier.set("full")

    val modules = nmsModules.flatMap { it.value }

    dependencies {
        include(project(":abstraction"))
        modules.forEach { include(project(it)) }
    }
}

tasks.register<ShadowJar>("shadowJar-latest") {
    from(sourceSets.main.get().output)
    archiveClassifier.set("latest")
    configurations = listOf(project.configurations.runtimeClasspath.get())

    val modules = nmsModules.tailMap(nmsModules.lastKey()).flatMap { it.value }

    dependencies {
        include(project(":abstraction"))
        modules.forEach { include(project(it)) }
    }
}

for (minVersion in (nmsModules.firstKey() + 1) until nmsModules.lastKey()) {
    tasks.register<ShadowJar>("shadowJar-from-$minVersion") {
        from(sourceSets.main.get().output)
        archiveClassifier.set("from-$minVersion")
        configurations = listOf(project.configurations.runtimeClasspath.get())

        val modules = nmsModules.tailMap(minVersion).flatMap { it.value }

        dependencies {
            include(project(":abstraction"))
            modules.forEach { include(project(it)) }
        }
    }
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
                    }"
                )

            credentials(PasswordCredentials::class)
            authentication { create<BasicAuthentication>("basic") }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            artifact(tasks.getByName("javadocJar"))
            artifact(tasks.getByName("sourcesJar"))
            tasks.filterIsInstance<ShadowJar>().forEach {
                artifact(it)
            }
        }
    }
}
