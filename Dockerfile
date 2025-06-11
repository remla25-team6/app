# Minimal base stage to copy pre-built JAR
FROM alpine:latest AS jar-copier
WORKDIR /app
COPY target/*.jar app.jar

# Slimmer runtime environment
FROM eclipse-temurin:17-jre-alpine-3.21
WORKDIR /app
COPY --from=jar-copier /app/app.jar .
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]