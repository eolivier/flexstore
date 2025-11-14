plugins {
    kotlin("jvm")
    id("org.springframework.boot") version "3.3.5"
    id("io.spring.dependency-management") version "1.1.5"

    kotlin("plugin.spring") version "1.9.25"
    kotlin("plugin.jpa") version "1.9.25"
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
    implementation("org.springframework.boot:spring-boot-starter-web")

    // JPA + Hibernate
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

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