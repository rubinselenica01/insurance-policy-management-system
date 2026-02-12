#Compile the Java application and create the JAR file
FROM maven:3.9-eclipse-temurin-21-alpine AS build

# Set working directory inside the container
WORKDIR /app

# Copy Maven configuration files
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy the entire source code
COPY src ./src

# Build the application
# -DskipTests: Skip tests during Docker build (tests should run in CI/CD)
# -B: Batch mode (non-interactive, better for containers)
# clean: Remove previous build artifacts
# package: Compile, test, and package the application into a JAR
RUN mvn clean package -DskipTests -B

#Create a minimal image with only the JAR file (no Maven, no source code)
FROM eclipse-temurin:21-jre-alpine

# Set working directory
WORKDIR /app

# Create a non-root user for security
# Running as root in containers is a security risk
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy only the built JAR from the build stage
# This keeps the final image small (no Maven, no source code, no build tools)
COPY --from=build /app/target/policy-management-service-0.0.1-SNAPSHOT.jar app.jar

# Expose the port that Spring Boot runs on
EXPOSE 8080

# Set JVM options for container environment
# -XX:+UseContainerSupport: JVM respects container memory limits
# -XX:MaxRAMPercentage=75.0: Use max 75% of container memory for heap
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Run the application
# ${JAVA_OPTS}: Apply JVM options from environment variable
# -jar app.jar: Execute the Spring Boot JAR
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
