FROM openjdk:8-jdk-slim

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar", "--logging.file.name=/app/logs/api-portal.log"]