# Usa uma imagem base com o Java 17
FROM openjdk:17-jdk-alpine

# Define o diretório de trabalho dentro do contêiner
WORKDIR /app

# Copia o arquivo JAR construído pela aplicação
COPY target/dateca-0.0.2.jar /app/dateca.jar

# Expõe a porta 8080 (a porta padrão do Spring Boot)
EXPOSE 8080

# Comando para executar a aplicação Spring Boot quando o contêiner for iniciado
CMD ["java", "-jar", "dateca-0.0.2.jar"]
