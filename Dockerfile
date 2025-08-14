FROM eclipse-temurin:21-jdk AS build

RUN ./gradlew dependencies --no-daemon

COPY . .

RUN ./gradlew build --no-daemon

FROM eclipse-temurin:21-jdk

COPY --from=build build/libs/app-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]