FROM golang:1.24 AS builder
WORKDIR /app/GoMailer
COPY . .
RUN ["go", "install", "github.com/swaggo/swag/cmd/swag@v1.3.2"]
RUN ["swag", "init", "-g", "./cmd/mailer/main.go", "-d", "./cmd/mailer,./internal,./pkg", "-o", "./docs", "--parseDependency", "--parseInternal"]
RUN ["go", "mod", "download"]
RUN ["env", "CGO_ENABLED=0", "GOOS=linux", "go", "build", "-a", "-installsuffix", "cgo", "-o", "app", "./cmd/mailer"]

FROM scratch
COPY --from=builder /app/GoMailer/app .
EXPOSE 8080
ENV GIN_MODE release
ENTRYPOINT ["./app"]
