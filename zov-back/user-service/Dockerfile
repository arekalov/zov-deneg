FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy built application from local build
COPY build/install/user-service ./

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD (echo > /dev/tcp/localhost/8080) 2>/dev/null && exit 0 || exit 1

# Run application
ENTRYPOINT ["./bin/user-service"]
