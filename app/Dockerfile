FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY app .

RUN ./gradlew dependencies --no-daemon

COPY . .

RUN ./gradlew build --no-daemon

FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY --from=build /app/build/libs/app-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]