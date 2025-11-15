FROM maven:3.8.5-openjdk-17 AS builder

WORKDIR /app


COPY backend/pom.xml .

RUN mvn dependency:go-offline

COPY backend/src ./src

RUN mvn clean install -DskipTests


FROM openjdk:17-jre-slim

WORKDIR /app

COPY --from=builder /app/target/qualaboa-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]