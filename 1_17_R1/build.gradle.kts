plugins { `java-library` }

dependencies {
    implementation("org.spigotmc:spigot:1.17-R0.1-SNAPSHOT")

    implementation(project(":1_17_1_R1"))
    api(project(":abstraction"))
}

java {
    targetCompatibility = JavaVersion.VERSION_16
    sourceCompatibility = JavaVersion.VERSION_16
}
