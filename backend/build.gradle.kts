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

    kotlin {
        jvmToolchain(21)
    }
}

// A line of code is 113 characters at most (fluent-code-reading-semantic skill). As for the front (ESLint
// max-len with ignoreStrings), a line that holds a string literal is not checked. The migrations up to V18 are
// already applied and frozen (Flyway checks their checksum): only the next ones are checked
val maxLineLength = 113
val lastFrozenMigration = 18

tasks.register("checkLineLength") {
    group = "verification"
    description = "Fails when a line of the Kotlin sources or of a new migration is too long"
    val kotlinSources = fileTree(projectDir) { include("**/src/**/*.kt"); exclude("**/build/**") }
    val migrations = fileTree(projectDir) { include("migrations/src/main/resources/db/migration/*.sql") }
    inputs.files(kotlinSources, migrations)
    // Local copies: the configuration cache does not keep references to the script
    val limit = maxLineLength
    val frozen = lastFrozenMigration
    val root = projectDir
    doLast {
        val newMigrations = migrations.filter { file ->
            val version = Regex("""^V(\d+)__""").find(file.name)?.groupValues?.get(1)?.toInt() ?: Int.MAX_VALUE
            version > frozen
        }
        val tooLong = (kotlinSources.files + newMigrations.files).flatMap { file ->
            file.readLines().withIndex()
                .filter { (_, line) -> line.length > limit && !line.contains('"') }
                .map { (index, line) -> "${file.relativeTo(root)}:${index + 1} (${line.length} characters)" }
        }
        if (tooLong.isNotEmpty()) {
            throw GradleException("Lines longer than $limit characters:\n" + tooLong.joinToString("\n"))
        }
    }
}
