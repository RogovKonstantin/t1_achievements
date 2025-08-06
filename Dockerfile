# Используем минимальный JDK-образ
FROM eclipse-temurin:21-jdk-alpine

# Папка для jar
WORKDIR /app

# Копируем jar (предполагаем, что ты собрал его в target/)
ARG JAR_FILE=target/achievements-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

# Запускаем
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
