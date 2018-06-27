import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


buildscript {
    val _kotlin_version = "1.2.50"

    repositories {
        mavenCentral()
        mavenLocal()
        jcenter()
    }

    dependencies {
        classpath(kotlin("gradle-plugin", _kotlin_version))
    }

}

subprojects {

    group = "pt.um.tf.lab3"
    version = "1.0-SNAPSHOT"

    apply{
        plugin("kotlin")
    }


    extra["bc_version"] = "1.59.1"
    extra["spread_version"] = "4.4.0"
    extra["kotlin_version"] = "1.2.50"
    extra["catalyst_version"] = "1.2.1"
    extra["ekit_version"] = "1.2-SNAPSHOT"
    extra["slf4j_version"] = "1.8.0-beta2"
    extra["kotlinlog_version"] = "1.5.4"

    repositories {
        mavenCentral()
        mavenLocal()
        jcenter()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }


}

