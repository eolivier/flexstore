# ---- Build stage ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# 1) Gradle copy
COPY backend/gradlew ./gradlew
COPY backend/gradle ./gradle
COPY backend/settings.gradle* ./
COPY backend/build.gradle* ./
COPY backend/gradle.properties ./
RUN chmod +x ./gradlew

# 2) Amorcer le wrapper + cache des dépendances Gradle
#    Grâce aux mounts BuildKit, le cache persiste entre builds
#    (caches Gradle + wrapper)
RUN --mount=type=cache,target=/root/.gradle/caches \
    --mount=type=cache,target=/root/.gradle/wrapper \
    ./gradlew --no-daemon -q help

# 3) Copier le reste du code (couche qui change souvent)
COPY backend/ ./

# 4) Lancer uniquement les tests unitaires (pas les *IT)
RUN --mount=type=cache,target=/root/.gradle/caches \
    --mount=type=cache,target=/root/.gradle/wrapper \
    ./gradlew test --no-daemon

# 5) Construire le JAR
RUN --mount=type=cache,target=/root/.gradle/caches \
    --mount=type=cache,target=/root/.gradle/wrapper \
    ./gradlew :spring-infra:bootJar --no-daemon

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/spring-infra/build/libs/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
