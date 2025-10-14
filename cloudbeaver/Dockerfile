# Build a CloudBeaver CE image with MongoDB JDBC baked in
# Pin a version to avoid surprises
FROM dbeaver/cloudbeaver:25.0.4

# ---- MongoDB JDBC driver version ----
ARG MONGO_JDBC_VERSION=3.0.2
ENV MONGO_JDBC_VERSION=${MONGO_JDBC_VERSION}

# Add the (fat) JDBC jar to CloudBeaver's drivers dir
# This path is where CloudBeaver expects driver jars
RUN set -eux; \
    mkdir -p /opt/cloudbeaver/server/drivers/mongodb-jdbc; \
    curl -fsSL -o /opt/cloudbeaver/server/drivers/mongodb-jdbc/mongodb-jdbc-${MONGO_JDBC_VERSION}-all.jar \
      https://repo1.maven.org/maven2/org/mongodb/mongodb-jdbc/${MONGO_JDBC_VERSION}/mongodb-jdbc-${MONGO_JDBC_VERSION}-all.jar

# Register the driver bundle/resources and enable it
# (You maintain this plugin.xml in your repo; see below)
COPY plugin.xml /opt/cloudbeaver/server/bundles/io.cloudbeaver.resources.drivers.base/plugin.xml

# Provide a DBeaver driver descriptor so the driver shows up in the list
COPY drivers/mongodb-jdbc/driver-mongodb-jdbc.xml /opt/cloudbeaver/server/conf/drivers/driver-mongodb-jdbc.xml

# Optional: keep your env flag
ENV CB_EXTENSIONS_DEFAULT=1
