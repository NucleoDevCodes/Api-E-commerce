FROM eclipse-temurin:17-jdk AS builder

WORKDIR /application

RUN apt-get update && apt-get install -y bash

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN chmod +x mvnw

# Agora sim, executa com bash
RUN bash ./mvnw clean package -DskipTests

# Etapa de runtime
FROM eclipse-temurin:17-jre

WORKDIR /application

COPY --from=builder /application/target/*.jar application.jar

ENTRYPOINT ["java", "-jar", "application.jar"]
