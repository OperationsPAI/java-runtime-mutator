# Example Dockerfile modification for TrainTicket services
# Add this to any TrainTicket service Dockerfile to enable runtime mutation

FROM maven:3.9.5-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Build stage for mutator agent
FROM maven:3.9.5-eclipse-temurin-17 AS mutator-builder
WORKDIR /mutator
COPY --from=mutator-repo /java-runtime-mutator/agent/pom.xml ./pom.xml
COPY --from=mutator-repo /java-runtime-mutator/agent/src ./src
RUN mvn clean package

# Final stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy application
COPY --from=builder /app/target/*.jar app.jar

# Copy mutator agent
COPY --from=mutator-builder /mutator/target/mutator-agent.jar /app/mutator-agent.jar

# Copy mutation configuration (optional)
COPY mutation-config.yaml /app/mutation-config.yaml

# Set Java agent via environment variable
ENV JAVA_TOOL_OPTIONS="-javaagent:/app/mutator-agent.jar=config=/app/mutation-config.yaml,port=8080"

EXPOSE 8080 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
