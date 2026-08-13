# ---------- BUILD ----------
FROM docker.io/rust:1-slim-bookworm AS build

ARG pkg=rocket-app
WORKDIR /build

RUN apt-get update && apt-get install -y --no-install-recommends \
    pkg-config libssl-dev ca-certificates build-essential binutils \
 && rm -rf /var/lib/apt/lists/*

COPY . .

RUN --mount=type=cache,target=/build/target \
    --mount=type=cache,target=/usr/local/cargo/registry \
    --mount=type=cache,target=/usr/local/cargo/git \
    set -eux; \
    cargo build --release; \
    objcopy --compress-debug-sections target/release/$pkg ./main

# ---------- RUNTIME ----------
FROM docker.io/debian:bookworm-slim
WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
    ca-certificates libssl3 \
 && rm -rf /var/lib/apt/lists/*

COPY --from=build /build/main ./

ENV ROCKET_ADDRESS=0.0.0.0
ENV ROCKET_PORT=8080
CMD ["./main"]
