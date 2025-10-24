FROM gradle:6.9.0 AS builder
WORKDIR /usr/src/app

COPY build.gradle .
COPY src ./src

RUN gradle bootJar --no-daemon

FROM openjdk:17-alpine
RUN apk add --no-cache curl

ARG JAR_FILE=build/libs/*.jar
COPY --from=builder /usr/src/app/${JAR_FILE} app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]