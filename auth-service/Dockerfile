#/**
# * Created by IntelliJ IDEA.
# *
# * User: @Fube
# * Date: 21/09/21
# * Ticket: feat(AUTH-CPC-59)
# *
# */
FROM gradle:6 as builder
WORKDIR /usr/src/app
COPY src ./src
COPY build.gradle .
RUN ["gradle", "bootJar"]

FROM openjdk:8-alpine
ARG JAR_FILE=build/libs/*.jar
COPY --from=builder /usr/src/app/${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar","/app.jar"]
