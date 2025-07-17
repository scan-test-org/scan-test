FROM openjdk:17-jdk-bullseye

WORKDIR /app

COPY target/portal-bootstrap-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"] 