FROM golang:1.24.0 AS builder
WORKDIR /app/GoMailer
COPY go.mod go.sum ./
RUN go mod download
COPY . .
RUN go install github.com/swaggo/swag/cmd/swag@v1.16.6
RUN swag init -g cmd/mailer/main.go -o ./docs
RUN CGO_ENABLED=0 GOOS=linux go build -a -installsuffix cgo -o app ./cmd/mailer

FROM scratch
COPY --from=builder /app/GoMailer/app .
EXPOSE 8080
ENV GIN_MODE=release
ENTRYPOINT ["./app"]
