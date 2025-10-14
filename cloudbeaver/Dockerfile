# Dockerfile
# Pin a CloudBeaver version you’re actually running
FROM dbeaver/cloudbeaver:25.2.3

# ---- Add MongoDB JDBC driver JAR(s)
ARG MONGO_JDBC_VERSION=3.0.2
RUN mkdir -p /opt/cloudbeaver/server/drivers/mongodb-jdbc && \
    curl -L -o /opt/cloudbeaver/server/drivers/mongodb-jdbc/mongodb-jdbc-${MONGO_JDBC_VERSION}.jar \
      https://repo1.maven.org/maven2/org/mongodb/mongodb-jdbc/${MONGO_JDBC_VERSION}/mongodb-jdbc-${MONGO_JDBC_VERSION}.jar

# ---- Register the driver in the resources plugin
# We’ll replace the plugin.xml with a patched version that:
#  1) Adds <resource name="drivers/mongodb-jdbc"/>
#  2) Adds <bundle id="drivers.mongodb-jdbc" .../>
#  3) Adds <driver id="generic.mongodb-jdbc"/>
COPY plugin.xml /opt/cloudbeaver/server/bundles/io.cloudbeaver.resources.drivers.base/plugin.xml
