

group = "org.flexstore"

repositories {
    mavenCentral()
}

dependencies {
    // Logging facade only: the logging framework is chosen by the application (Spring Boot: Logback)
    implementation("org.slf4j:slf4j-api:2.0.16")

    testImplementation(project(":infra"))
    testImplementation("ch.qos.logback:logback-classic:1.5.11")

    testImplementation(kotlin("test"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.8.2")

    // Add Mockito dependency
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")

    testImplementation("io.mockk:mockk:1.13.2")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}