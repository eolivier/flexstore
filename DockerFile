# Utilise une image JDK officielle
FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

# Copie les fichiers de build
COPY . .

# Compile le projet
RUN ./gradlew build --no-daemon

# Utilise une image JRE légère pour l'exécution
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copie le jar généré
COPY --from=build /app/build/libs/*.jar app.jar

# Expose le port (modifie selon ton app)
EXPOSE 8080

# Commande de lancement
CMD ["java", "-jar", "app.jar"]