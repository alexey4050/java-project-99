FROM eclipse-temurin:21-jdk AS build

WORKDIR /app
COPY . .
RUN apk add --no-cache bash && \
    chmod +x gradlew && \
    ./gradlew build --no-daemon

FROM eclipse-temurin:21-jre-alpine
COPY --from=0 /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]