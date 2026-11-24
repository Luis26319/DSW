FROM eclipse-temurin:25-jre
WORKDIR /app
COPY target/backend-academico-0.0.12-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]