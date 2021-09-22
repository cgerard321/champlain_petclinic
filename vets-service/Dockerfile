FROM gradle:6 as builder
WORKDIR /usr/src/app
COPY src ./src
COPY build.gradle .
RUN ["gradle", "bootJar"]

EXPOSE 8080

<<<<<<< HEAD
FROM openjdk:8-alpine
=======
FROM openjdk:11
>>>>>>> dd9eb5d489ebdc95fbeea187a068f08d66cb1fdd
ARG JAR_FILE=build/libs/*.jar
COPY --from=builder /usr/src/app/${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar","/app.jar"]