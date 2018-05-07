dependencies {
    implementation(kotlinModule("stdlib-jdk8", project.extra["kotlin_version"] as String))
    compile("pt.haslab", "ekit", project.extra["ekit_version"] as String)
    compile(project(":lab3-mes"))
}

