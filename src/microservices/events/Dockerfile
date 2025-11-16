FROM maven:4.0.0-rc-5-eclipse-temurin-25 AS build

WORKDIR /build

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM maven:4.0.0-rc-5-eclipse-temurin-25

WORKDIR /app

COPY --from=build /build/target/events-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
