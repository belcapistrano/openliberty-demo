# Multi-stage build for optimal image size
FROM maven:3.9-eclipse-temurin-17 AS builder

# Set working directory
WORKDIR /app

# Copy Maven files first (for better layer caching)
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Production stage using Open Liberty base image
FROM icr.io/appcafe/open-liberty:24.0.0.10-kernel-slim-java17-openj9-ubi

# Copy the WAR file to the Liberty server
COPY --from=builder /app/target/openliberty-demo.war /opt/ol/wlp/usr/servers/defaultServer/apps/

# Copy server configuration
COPY --from=builder /app/src/main/liberty/config/server.xml /opt/ol/wlp/usr/servers/defaultServer/

# Create necessary directories and set permissions
USER root
RUN mkdir -p /opt/ol/wlp/usr/servers/defaultServer/logs \
    && chown -R 1001:0 /opt/ol/wlp/usr/servers/defaultServer \
    && chmod -R g+rw /opt/ol/wlp/usr/servers/defaultServer

# Switch back to Liberty user
USER 1001

# Configure environment variables
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV WLP_OUTPUT_DIR=/opt/ol/wlp/usr/servers/defaultServer
ENV WLP_LOGGING_CONSOLE_LOGLEVEL=info
ENV WLP_LOGGING_CONSOLE_SOURCE=message,trace,accessLog,ffdc,audit

# Expose port
EXPOSE 9080 9443

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:9080/openliberty-demo/api/health || exit 1

# Configure the server to run
RUN configure.sh