FROM openjdk:17
WORKDIR /app
COPY target/service-b.jar service-b.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "service-b.jar"]
