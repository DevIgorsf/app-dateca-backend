# Usa uma imagem base com o Java 17
FROM maven:3.8.1-openjdk-17 AS build
# Define o diretório de trabalho dentro do contêiner
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -X -f /home/app/pom.xml clean package

# Copia o arquivo JAR construído pela aplicação
FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY target/dateca-0.0.2.jar /app/dateca-0.0.2.jar
# Expõe a porta 8080 (a porta padrão do Spring Boot)
EXPOSE 8080
# Comando para executar a aplicação Spring Boot quando o contêiner for iniciado
CMD ["java", "-jar", "dateca-0.0.2.jar"]
