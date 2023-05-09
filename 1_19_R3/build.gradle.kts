plugins { `java-library` }

dependencies {
    implementation("org.spigotmc:spigot:1.19.4-R0.1-SNAPSHOT")

    api(project(":abstraction"))
}

java {
    targetCompatibility = JavaVersion.VERSION_17
    sourceCompatibility = JavaVersion.VERSION_17
}
