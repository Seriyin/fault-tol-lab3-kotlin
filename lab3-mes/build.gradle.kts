dependencies {
    implementation(kotlinModule("stdlib-jdk8", project.extra["kotlin_version"] as String))
    compile("io.atomix.catalyst", "catalyst-serializer", project.extra["catalyst_version"] as String)
    compile("io.atomix.catalyst", "catalyst-transport", project.extra["catalyst_version"] as String)
    compile("org.spread","spread", project.extra["spread_version"] as String)
}

