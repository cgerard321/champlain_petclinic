# cloudbeaver-mongo/Dockerfile
ARG BASE_TAG=latest
FROM dbeaver/cloudbeaver:${BASE_TAG}

# --- A) Put MongoDB JDBC jars into driversLocation (wiki requirement)
ARG MONGO_JDBC_VERSION=3.0.2
RUN set -eux; \
    mkdir -p /opt/cloudbeaver/drivers/mongodb-jdbc; \
    curl -fsSL -o /opt/cloudbeaver/drivers/mongodb-jdbc/mongodb-jdbc-${MONGO_JDBC_VERSION}.jar \
      https://repo1.maven.org/maven2/org/mongodb/mongodb-jdbc/${MONGO_JDBC_VERSION}/mongodb-jdbc-${MONGO_JDBC_VERSION}.jar

# --- B) Patch the resources plugin (adds resource/bundle/enabled)
#     (This tells CloudBeaver where jars are and enables the full driver id)
COPY plugin.resources.xml /opt/cloudbeaver/server/bundles/io.cloudbeaver.resources.drivers.base/plugin.xml

# --- C) Patch the Generic provider plugin (adds the driver definition itself)
#     (This is the critical step: define com.mongodb.jdbc.MongoDriver under Generic)
COPY plugin.generic.xml /opt/cloudbeaver/server/bundles/org.jkiss.dbeaver.ext.generic/plugin.xml
