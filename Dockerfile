# ── Build stage ──────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN ./mvnw -q -B package -DskipTests 2>/dev/null || \
    (apk add --no-cache maven && mvn -q -B package -DskipTests)

# ── Runtime stage ─────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/portfolio-api-*.jar app.jar

# Non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring \
 && mkdir -p /app/data && chown -R spring:spring /app/data
USER spring

# JSON data files live here — mount a volume to keep them across deploys
ENV PORTFOLIO_DATA_DIR=/app/data
VOLUME ["/app/data"]

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
