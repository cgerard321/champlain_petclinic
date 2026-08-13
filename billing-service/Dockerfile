FROM gradle:6.9.0 AS builder
WORKDIR /usr/src/app
COPY build.gradle settings.gradle ./

COPY src ./src

RUN gradle bootJar --no-daemon && \
    mv build/libs/*.jar build/libs/application.jar

FROM eclipse-temurin:17-jre-alpine

RUN apk add --no-cache curl && \
    addgroup -S spring && \
    adduser -S spring -G spring

COPY --from=builder /usr/src/app/build/libs/application.jar app.jar

USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java", \
    "-XX:+UseG1GC", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+ExitOnOutOfMemoryError", \
    "-XX:MaxMetaspaceSize=256m", \
    "-XX:+UseStringDeduplication", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Xss512k", \
    "-jar", \
    "app.jar"]