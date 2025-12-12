plugins {
    kotlin("jvm")
}

group = "org.flexstore"
version = "infra"

dependencies {
    implementation(project(":domain"))
    
    // Security - Password Encryption (non-Spring BCrypt library)
    implementation("at.favre.lib:bcrypt:0.10.2")
    
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}