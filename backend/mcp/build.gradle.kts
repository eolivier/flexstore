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

dependencies {
    // The tools call the use cases of the domain
    implementation(project(":domain"))
    // The messages of the Model Context Protocol are JSON-RPC (the version is the one of Spring Boot in spring-infra)
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.5")

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
