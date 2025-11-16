FROM maven:4.0.0-rc-5-eclipse-temurin-25 AS build

WORKDIR /build

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM maven:4.0.0-rc-5-eclipse-temurin-25

WORKDIR /app

COPY --from=build /build/target/proxy-1.0-SNAPSHOT-jar-with-dependencies.jar /app/app.jar

EXPOSE 8000

ENTRYPOINT ["java", "-jar", "app.jar"]
