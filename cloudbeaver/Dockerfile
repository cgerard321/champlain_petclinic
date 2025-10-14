# cloudbeaver-mongo/Dockerfile
ARG BASE_TAG=latest
FROM dbeaver/cloudbeaver:${BASE_TAG}

# --- 1) Put MongoDB JDBC jars into the drivers *storage* (per wiki)
# driversLocation defaults to /opt/cloudbeaver/drivers
ARG MONGO_JDBC_VERSION=3.0.2
RUN set -eux; \
    mkdir -p /opt/cloudbeaver/drivers/mongodb-jdbc; \
    curl -fsSL -o /opt/cloudbeaver/drivers/mongodb-jdbc/mongodb-jdbc-${MONGO_JDBC_VERSION}.jar \
      https://repo1.maven.org/maven2/org/mongodb/mongodb-jdbc/${MONGO_JDBC_VERSION}/mongodb-jdbc-${MONGO_JDBC_VERSION}.jar

# --- 2) Register resource/bundle/driver id (per wiki “Include driver in server configuration”)
# Your plugin.xml (below) adds:
#   <resource name="drivers/mongodb-jdbc"/>
#   <bundle id="drivers.mongodb-jdbc" label="MongoDB JDBC Driver files"/>
#   <driver id="generic.mongodb-jdbc"/>
COPY plugin.xml /opt/cloudbeaver/server/bundles/io.cloudbeaver.resources.drivers.base/plugin.xml
