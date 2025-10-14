FROM gradle:7.4-jdk17 AS builder
WORKDIR /usr/src/app
COPY build.gradle settings.gradle ./
COPY src ./src
RUN gradle bootJar --no-daemon
RUN ls -l build/libs && mv build/libs/*.jar build/libs/application.jar


FROM gcr.io/distroless/java17-debian11 AS final
WORKDIR /app
ENV JAVA_TOOL_OPTIONS=" \
    -XX:+UseG1GC \
    -XX:MaxRAMPercentage=75.0 \
    -XX:+UseStringDeduplication \
    -XX:MaxMetaspaceSize=256m \
    -Xss512k \
    "
COPY --from=builder /usr/src/app/build/libs/application.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]