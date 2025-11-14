plugins {
    kotlin("jvm")
}

dependencies {
    // aucune dépendance obligatoire ici
}

tasks.test { enabled = false }  // pas de tests ici

// publication d’un simple jar de ressources (pour être visible via runtimeOnly)
