dependencies {
    implementation(kotlin("stdlib", project.extra["kotlin_version"] as String))
    implementation("io.atomix.catalyst", "catalyst-serializer", project.extra["catalyst_version"] as String)
    implementation("io.atomix.catalyst", "catalyst-transport", project.extra["catalyst_version"] as String)
}
