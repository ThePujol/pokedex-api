# Estágio 1: Build (Compilação)
# MUDANÇA: Usando uma imagem Maven com Java 21 (Eclipse Temurin)
FROM maven:3-eclipse-temurin-21-alpine AS build

# Copia todo o código do projeto para dentro do container
WORKDIR /app
COPY . .

# Roda o comando do Maven para compilar e criar o .jar
RUN mvn clean package -DskipTests

# Estágio 2: Run (Execução)
# MUDANÇA: Usando uma imagem JRE (Java Runtime) 21 para rodar o app
FROM openjdk:21-slim

# Expõe a porta 8080 (a porta que o Spring Boot usa)
EXPOSE 8080

# Copia o .jar que foi compilado no Estágio 1 para este container
# (O nome do .jar vem do seu pom.xml e está correto)
COPY --from=build /app/target/pokedex-api-0.0.1-SNAPSHOT.jar /app/pokedex-api.jar

# Define o comando que o Render irá executar para iniciar o servidor
ENTRYPOINT ["java", "-jar", "/app/pokedex-api.jar"]