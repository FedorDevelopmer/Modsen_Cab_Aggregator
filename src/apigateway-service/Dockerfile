FROM eclipse-temurin:17-jdk-jammy
RUN apt-get update && apt-get install -y netcat
WORKDIR /app
COPY target/apigateway-service-0.0.1-SNAPSHOT.jar /app/main-module.jar
ENTRYPOINT ["java", "-jar", "main-module.jar"]