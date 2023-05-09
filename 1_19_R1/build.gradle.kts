plugins { `java-library` }

dependencies {
    implementation("org.spigotmc:spigot:1.19-R0.1-SNAPSHOT")

    implementation(project(":1_19_1_R1"))
    api(project(":abstraction"))
}

java {
    targetCompatibility = JavaVersion.VERSION_17
    sourceCompatibility = JavaVersion.VERSION_17
}
