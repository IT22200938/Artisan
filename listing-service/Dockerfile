FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN adduser -D appuser
USER appuser
COPY target/listing-service-*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
