FROM maven:3.8.1-openjdk-17 AS build

COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package

FROM openjdk:17-jdk-alpine
WORKDIR /app
COPY --from=build /home/app/target/dateca-0.0.2.jar /app/dateca.jar
EXPOSE 80
CMD ["java", "-jar", "/app/dateca.jar"]


