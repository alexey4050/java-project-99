FROM eclipse-temurin:21-jdk AS build

# Создаём рабочую директорию в контейнере
WORKDIR /app

# Копируем ВСЕ файлы из текущей директории
# В директорию /app внутри контейнера
COPY . .

# Теперь все команды выполняются в /app
RUN ./gradlew build --no-daemon

# Финальный образ
FROM eclipse-temurin:21-jre-alpine

# Создаём рабочую директорию
WORKDIR /app

# Копируем JAR из стадии build
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]