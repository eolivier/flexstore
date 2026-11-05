plugins {
    kotlin("jvm")
}

group = "org.flexstore"
version = "mcp"

repositories {
    mavenCentral()
}

// The root build gives springdoc to every module: the MCP adapter needs no Spring
configurations.all {
    exclude(group = "org.springdoc")
}

val mcpSdkVersion: String by project

dependencies {
    // The tools call the use cases of the domain
    implementation(project(":domain"))
    // The Model Context Protocol is served by its Java SDK, the tools are described with its types
    implementation("io.modelcontextprotocol.sdk:mcp-core:$mcpSdkVersion")
    // The tools answer with their products in JSON (the version is the one the SDK needs)
    implementation("com.fasterxml.jackson.core:jackson-databind:2.20.1")

    testImplementation(project(":infra"))
    testImplementation("io.mockk:mockk:1.13.2")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
