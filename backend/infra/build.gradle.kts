plugins {
    kotlin("jvm")
}

group = "org.flexstore"
version = "infra"

dependencies {
    implementation(project(":domain"))
    
    // Security - Password Encryption (non-Spring BCrypt library)
    implementation("at.favre.lib:bcrypt:0.10.2")

    // Reading of the WebP images given by the administrators (ImageIO reads JPEG and PNG on its own)
    implementation("com.twelvemonkeys.imageio:imageio-webp:3.12.0")
    
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
}

tasks.test {
    useJUnitPlatform()
}