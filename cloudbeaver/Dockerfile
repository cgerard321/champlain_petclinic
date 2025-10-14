# cloudbeaver-mongo/Dockerfile
ARG BASE_TAG=latest
FROM dbeaver/cloudbeaver:${BASE_TAG}

ARG MONGO_JDBC_VERSION=3.0.2
RUN mkdir -p /opt/cloudbeaver/server/drivers/mongodb-jdbc && \
    curl -L -o /opt/cloudbeaver/server/drivers/mongodb-jdbc/mongodb-jdbc-${MONGO_JDBC_VERSION}.jar \
      https://repo1.maven.org/maven2/org/mongodb/mongodb-jdbc/${MONGO_JDBC_VERSION}/mongodb-jdbc-${MONGO_JDBC_VERSION}.jar

# plugin.xml is your patched file that adds:
#   <resource name="drivers/mongodb-jdbc"/>
#   <bundle id="drivers.mongodb-jdbc" .../>
#   <driver id="generic.mongodb-jdbc"/>
COPY plugin.xml /opt/cloudbeaver/server/bundles/io.cloudbeaver.resources.drivers.base/plugin.xml
