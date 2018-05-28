dependencies {
    //Maybe no need for bouncy castle API
    //implementation("org.bouncycastle", "bcprov-jdk15on", project.extra["bc_version"] as String)
    implementation(kotlinModule("stdlib-jdk8", project.extra["kotlin_version"] as String))
    compile("pt.haslab", "ekit", project.extra["ekit_version"] as String)
    compile(project(":lab3-mes"))
}

