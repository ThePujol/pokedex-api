# Estágio 1: Build (Compilação)
# Usamos uma imagem oficial do Maven (que tem Java) para compilar o projeto
FROM maven:3.8.5-openjdk-17-slim AS build

# Copia todo o código do projeto para dentro do container
WORKDIR /app
COPY . .

# Roda o comando do Maven para compilar e criar o .jar
RUN mvn clean package -DskipTests

# Estágio 2: Run (Execução)
# Usamos uma imagem Java "limpa" (menor) para rodar o app
FROM openjdk:17-jre-slim

# Expõe a porta 8080 (a porta que o Spring Boot usa)
EXPOSE 8080

# Copia o .jar que foi compilado no Estágio 1 para este container
COPY --from=build /app/target/pokedex-api-0.0.1-SNAPSHOT.jar /app/pokedex-api.jar

# Define o comando que o Render irá executar para iniciar o servidor
ENTRYPOINT ["java", "-jar", "/app/pokedex-api.jar"]