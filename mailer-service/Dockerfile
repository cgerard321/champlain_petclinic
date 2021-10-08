FROM golang:1.17.1 AS builder
WORKDIR /app/GoMailer
COPY . .
RUN ["go", "install", "github.com/swaggo/swag/cmd/swag@latest"]
RUN ["swag", "init"]
RUN ["go", "get", "-d", "-v", "./..."]
RUN CGO_ENABLED=0 GOOS=linux go build -a -installsuffix cgo -o app .

FROM scratch
COPY --from=builder /app/GoMailer/app .
EXPOSE 8080
ENV GIN_MODE release
ENTRYPOINT ["./app"]