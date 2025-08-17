FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

RUN ./gradlew dependencies --no-daemon

COPY . .

RUN chmod +x gradlew || true

RUN ./gradlew build --no-daemon

FROM eclipse-temurin:21-jdk

COPY --from=build build/libs/app-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]