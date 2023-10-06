plugins {
  `java-library`
  alias(libs.plugins.shadow)
  alias(libs.plugins.runPaper)
}

dependencies {
  implementation(libs.paper)
  implementation(project(":anvilgui"))
}

tasks {
  runServer { minecraftVersion(libs.versions.paper.get().substringBefore('-')) }
  compileJava { dependsOn(":spotlessCheck") }
  shadowJar {
    archiveClassifier.set("")

    dependencies { include(project(":anvilgui")) }
  }
}

java { toolchain { languageVersion = JavaLanguageVersion.of(17) } }
