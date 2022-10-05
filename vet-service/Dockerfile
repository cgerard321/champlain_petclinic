FROM gradle:7.3.3 as builder
WORKDIR /usr/src/app
COPY src ./src
COPY build.gradle .
RUN ["gradle", "bootJar"]

EXPOSE 8080

FROM openjdk:17-alpine
ARG JAR_FILE=build/libs/*.jar
COPY --from=builder /usr/src/app/${JAR_FILE} app.jar
RUN apk update; apk add curl
ENTRYPOINT ["java", "-jar", "/app.jar"]