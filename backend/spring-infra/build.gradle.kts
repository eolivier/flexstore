plugins {
    kotlin("jvm")
    id("org.springframework.boot") version "3.4.13"
    id("io.spring.dependency-management") version "1.1.5"

    kotlin("plugin.spring") version "2.2.21"
    kotlin("plugin.jpa") version "2.2.21"
}

group = "org.flexstore"
version = "spring-infra"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))
    implementation(project(":domain"))
    implementation(project(":infra"))
    // The MCP server that exposes the products to AI assistants, served by this application
    implementation(project(":mcp"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-mail")

    // Security - authentication with JWT bearer tokens, password encryption
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.security:spring-security-crypto")
    implementation("org.bouncycastle:bcprov-jdk18on:1.78")

    // Payment provider (Stripe Checkout); without a secret key, the payments are simulated
    implementation("com.stripe:stripe-java:28.2.0")

    // JPA + Hibernate
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // Cache of the reads of the user table
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.github.ben-manes.caffeine:caffeine")

    // Driver PostgreSQL
    runtimeOnly("org.postgresql:postgresql")

    // Flyway
    runtimeOnly(project(":migrations"))
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")

    // Observability
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("io.micrometer:micrometer-core")

    // Tests
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(kotlin("test"))
    testImplementation("org.springframework.security:spring-security-test")

    // Driver pour exécuter les tests (unit/IT)
    testRuntimeOnly("org.postgresql:postgresql")
    // Migrations visibles pendant les tests si nécessaires
    testRuntimeOnly(project(":migrations"))
}

tasks.test {
    useJUnitPlatform()
    // do not run Integration Tests because they need db connection
    exclude("**/*IT.class", "**/*IT.*")
}

// 👉 Tâche qui réutilise le *même* sourceSet 'test' mais ne lance que les *IT
tasks.register<Test>("integrationTest") {
    description = "Runs integration tests (*IT) living under src/test"
    group = "verification"
    useJUnitPlatform()

    // Réutilise les outputs/classes & le classpath de 'test'
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath

    // Ne lance QUE les *IT
    include("**/*IT.class", "**/*IT.*")

    shouldRunAfter(tasks.test)
}

// Pour que `./gradlew check` lance aussi les IT
tasks.named("check") {
    dependsOn("integrationTest")
}
kotlin {
    jvmToolchain(21)
}