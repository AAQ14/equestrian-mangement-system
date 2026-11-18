# One Dockerfile for every microservice.
# SERVICE tells it which folder in services/ to build, for example: user-service.

# ---------- Stage 1: build the jar with Maven ----------
FROM maven:3.9-eclipse-temurin-17 AS build
ARG SERVICE
WORKDIR /app

# Copy the pom first, so the dependencies are cached between builds.
COPY services/${SERVICE}/pom.xml .
RUN mvn -q dependency:go-offline

# Copy the code and build the jar (tests are run later in Jenkins).
COPY services/${SERVICE}/src ./src
RUN mvn -q package -DskipTests

# ---------- Stage 2: a small image that only runs the jar ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]