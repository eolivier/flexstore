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
        implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
        implementation(kotlin("stdlib"))
        testImplementation(kotlin("test"))
        testImplementation("org.assertj:assertj-core:3.21.0")
    }
}
