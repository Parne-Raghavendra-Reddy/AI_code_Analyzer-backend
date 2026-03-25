# Stage 1: Build the application using Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy the pom.xml and download dependencies
# This caches the dependencies layer unless pom.xml changes
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source code and build the application
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create a minimal runtime image
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the built jar from the build stage (the wildcard handles the jar versioning name)
COPY --from=build /app/target/*.jar app.jar

# Expose the application port
EXPOSE 8090

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
