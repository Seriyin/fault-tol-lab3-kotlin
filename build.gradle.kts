import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


buildscript {
    val _kotlin_version = "1.2.41"

    repositories {
        mavenCentral()
        mavenLocal()
        jcenter()
    }

    dependencies {
        classpath(kotlinModule("gradle-plugin", _kotlin_version))
    }

}

subprojects {

    group = "pt.um.tf.lab2"
    version = "1.0-SNAPSHOT"

    apply {
        plugin("kotlin")
        plugin("application")
    }

    extra.set("bc_version", "1.59.1")
    extra.set("spread_version", "4.4.0")
    extra.set("kotlin_version", "1.2.41")
    extra.set("catalyst_version", "1.2.1")
    extra.set("ekit_version", "1.2-SNAPSHOT")

    repositories {
        mavenCentral()
        mavenLocal()
        jcenter()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.10"
    }
}

