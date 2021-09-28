FROM golang:1.17.1 AS builder
WORKDIR /app/GoMailer
COPY . .
RUN ["go", "get", "-d", "-v", "./..."]
RUN CGO_ENABLED=0 GOOS=linux go build -a -installsuffix cgo -o app .

FROM scratch
COPY --from=builder /app/GoMailer/app .
EXPOSE 8080
ENTRYPOINT ["./app"]