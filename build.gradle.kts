plugins {
    kotlin("jvm") version "1.9.23"
}

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    dependencies {
        implementation(kotlin("stdlib"))
        testImplementation(kotlin("test"))
    }
}
