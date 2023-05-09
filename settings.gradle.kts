import kotlin.io.path.forEachDirectoryEntry

rootProject.name = "anvilgui-parent"

include(":api")

project(":api").name = "anvilgui"

include(":abstraction")

rootDir.toPath().forEachDirectoryEntry("1_*R?") { include(":${it.fileName}") }

include(":test-plugin")