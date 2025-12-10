plugins {
    kotlin("jvm")
}

group = "org.flexstore"
version = "infra"

dependencies {
    implementation(project(":domain"))
    
    // Security - Password Encryption
    implementation("org.springframework.security:spring-security-crypto:6.3.4")
    implementation("org.bouncycastle:bcprov-jdk18on:1.78")
    
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}