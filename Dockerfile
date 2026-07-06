FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY pom.xml .
COPY backend/pom.xml backend/pom.xml
COPY frontend/package.json frontend/package-lock.json frontend/

RUN mvn -pl backend dependency:go-offline -DskipTests

COPY frontend frontend
COPY backend backend

RUN mvn -pl backend package -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app

ENV JAVA_OPTS=""
ENV APP_UPLOAD_DIR=/app/uploads

COPY --from=build /workspace/backend/target/dao-home-backend-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
