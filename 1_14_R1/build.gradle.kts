plugins { `java-library` }

dependencies {
    implementation("org.spigotmc:spigot:1.14-R0.1-SNAPSHOT")

    implementation(project(":1_14_4_R1"))
    api(project(":abstraction"))
}

java {
    targetCompatibility = JavaVersion.VERSION_1_8
    sourceCompatibility = JavaVersion.VERSION_1_8
}
