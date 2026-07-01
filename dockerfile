# ---- Build stage ----
FROM gradle:8.10-jdk21 AS build
WORKDIR /app

# Copia primeiro só os arquivos de config pra aproveitar cache de dependências
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY gradlew ./
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon || true

# Agora copia o restante do código e builda
COPY src ./src
RUN ./gradlew clean build -x test --no-daemon

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Usuário não-root (boa prática de segurança)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]