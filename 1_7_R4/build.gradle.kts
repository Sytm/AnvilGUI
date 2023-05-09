plugins { `java-library` }

dependencies {
    implementation("org.bukkit:craftbukkit:1.7.10-R0.1-SNAPSHOT")
    api(project(":abstraction"))
}

java {
    targetCompatibility = JavaVersion.VERSION_1_8
    sourceCompatibility = JavaVersion.VERSION_1_8
}
