FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN adduser -D appuser
USER appuser
COPY target/api-gateway-*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
