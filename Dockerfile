# Stage 1: Build
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends maven && rm -rf /var/lib/apt/lists/*

COPY pom.xml .
COPY src ./src
RUN mvn package -DskipTests -B

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN apk add --no-cache dumb-init

# Create non-root user
RUN adduser -D -g "" appuser

COPY --from=builder /app/target/*.jar app.jar
RUN chown -R appuser:appuser /app

USER appuser

EXPOSE 8080

ENTRYPOINT ["dumb-init", "--"]
CMD ["java", "-jar", "app.jar"]
