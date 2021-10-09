FROM gradle:6.9.0 as builder
WORKDIR /usr/src/app
COPY src ./src
COPY build.gradle .
RUN ["gradle", "bootJar"]

EXPOSE 8080

FROM openjdk:8-alpine
ARG JAR_FILE=build/libs/*.jar
COPY --from=builder /usr/src/app/${JAR_FILE} app.jar
RUN apk --no-cache add curl
ENTRYPOINT ["java", "-jar","/app.jar"]