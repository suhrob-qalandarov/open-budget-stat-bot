# OpenJDK 19 asosida image yaratamiz (Kotlin va Gradle uchun mos)
FROM openjdk:19-jdk-slim

# Ishchi direktoriyani sozlaymiz
WORKDIR /app

# Gradle wrapper va loyiha fayllarini ko'chirish
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle.kts
COPY src src

# Gradle wrapper uchun ruxsat beramiz
RUN chmod +x gradlew

# Loyihani build qilamiz (JAR faylini yaratish)
RUN ./gradlew build --no-daemon

# Ilovani ishga tushirish uchun JAR faylini ishlatamiz
CMD ["java", "-jar", "build/libs/op-stat-bot-0.0.1-SNAPSHOT.jar"]