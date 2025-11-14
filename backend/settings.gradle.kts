plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "flexstore"

include("app")
include("domain")
include("infra")
include("spring-infra")
include("migrations")
