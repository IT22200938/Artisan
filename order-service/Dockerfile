FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN adduser -D appuser
USER appuser
COPY target/order-service-*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
