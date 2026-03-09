FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN adduser -D appuser
USER appuser
COPY target/review-service-*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
