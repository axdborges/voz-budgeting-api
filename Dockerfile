# Etapa 1: build com Gradle (usa o wrapper, garante a versão correta do Gradle)
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

COPY gradlew ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew --version

COPY build.gradle settings.gradle ./
COPY src ./src

RUN ./gradlew bootJar --no-daemon

# Etapa 2: imagem final, apenas com o JRE e o jar gerado
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
