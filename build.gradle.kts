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
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
        testImplementation("org.junit.jupiter:junit-jupiter-params:5.8.2")
        testImplementation("org.assertj:assertj-core:3.21.0")
    }
}
