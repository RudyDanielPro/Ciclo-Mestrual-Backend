# Etapa 1: Compilar el JAR
FROM maven:3.9.8-eclipse-temurin-22 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Ejecutar la app
FROM eclipse-temurin:22-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080

# Render asigna un puerto dinámico mediante la variable $PORT
ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=${PORT:8080}"]