FROM gradle:7.4-jdk17 AS builder
WORKDIR /usr/src/app
COPY build.gradle settings.gradle ./
COPY src ./src
RUN gradle bootJar --no-daemon
RUN ls -l build/libs && mv build/libs/*.jar build/libs/application.jar


FROM openjdk:17-alpine
ARG JAR_FILE=build/libs/*.jar
COPY --from=builder /usr/src/app/${JAR_FILE} app.jar
#RUN apk --no-cache add curl
ENTRYPOINT ["java", "-jar","/app.jar"]