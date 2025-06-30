FROM openjdk:17
WORKDIR /app
COPY target/service-a.jar service-a.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "service-a.jar"]
