FROM maven:3.9.9-amazoncorretto-17-alpine AS build
WORKDIR /app
COPY src /app/src
COPY pom.xml /app
RUN mvn -f /app/pom.xml clean package -U

FROM amazoncorretto:17-alpine AS run
COPY --from=build /app/target/user-service-*.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]