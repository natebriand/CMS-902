# Uses an official lightweight JDK 22 image as the base.
FROM eclipse-temurin:22-jdk-alpine

# Creates a working directory inside the container.
WORKDIR /app

# Copies the built JAR from the host into the container.
COPY build/libs/CMS_902-1.0.0.jar app.jar

# When the container starts run the JAR.
ENTRYPOINT ["java", "-jar", "app.jar"]