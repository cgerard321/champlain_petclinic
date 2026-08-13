FROM golang:1.24 AS builder
WORKDIR /app/GoFiles
COPY . .
RUN go get -d -v ./...
RUN CGO_ENABLED=0 GOOS=linux go build -a -installsuffix cgo -o app ./cmd/files

FROM scratch
COPY --from=builder /app/GoFiles/app /
EXPOSE 8000
ENV GIN_MODE=release
ENTRYPOINT ["/app"]