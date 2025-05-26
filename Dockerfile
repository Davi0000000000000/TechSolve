# Etapa de build com Maven + Java 17
FROM maven:3.9.4-eclipse-temurin-17 AS builder

WORKDIR /app
COPY . .

# Compilar o projeto
RUN mvn clean package -DskipTests

# Etapa de execução com JDK leve
FROM eclipse-temurin:17-jdk

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
