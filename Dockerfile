# Build stage
FROM maven:3.8.1-openjdk-17 AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -X -f /home/app/pom.xml clean package

# Package stage
FROM openjdk:17
COPY --from=build /home/app/target/dateca-0.0.2.jar /usr/local/lib/dateca.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","-DDEBUG","/usr/local/lib/dateca.jar"]

