apply {
    plugin("application")
}

dependencies {
    implementation(kotlin("stdlib", project.extra["kotlin_version"] as String))
    implementation("pt.haslab","ekit",project.extra["ekit_version"] as String)
    implementation("org.slf4j", "slf4j-api", extra["slf4j_version"] as String)
    implementation("io.github.microutils", "kotlin-logging", extra["kotlinlog_version"] as String)
    runtime("org.slf4j", "slf4j-simple", extra["slf4j_version"] as String)
    implementation(project(":lab3-mes"))
}