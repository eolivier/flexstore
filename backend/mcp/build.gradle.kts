plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.2.21"
    application
}

group = "org.flexstore"
version = "mcp"

repositories {
    mavenCentral()
}

// The root build gives springdoc to every module: a server on the standard input and output has no use for Spring
configurations.all {
    exclude(group = "org.springdoc")
}

dependencies {
    // Model Context Protocol server, spoken on the standard input and output
    implementation("io.modelcontextprotocol:kotlin-sdk-server:0.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

    // The standard output carries the protocol: the logs go to the standard error (the default of slf4j-simple)
    runtimeOnly("org.slf4j:slf4j-simple:2.0.16")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass.set("org.flexstore.mcp.MainKt")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
