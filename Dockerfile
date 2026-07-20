# 1. Use a specific, updated tag on Temurin JRE (Debian bookworm)
FROM tomcat:9.0-jre21-temurin-jammy

LABEL Author="Europeana Foundation <development@europeana.eu>"
WORKDIR /usr/local/tomcat/webapps

# 2. Patch any OS-level security vulnerabilities at build time
RUN apt-get update && \
    apt-get upgrade -y && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# 3. Configure Elastic APM agent
ENV ELASTIC_APM_VERSION=1.52.1
ADD https://repo1.maven.org/maven2/co/elastic/apm/elastic-apm-agent/${ELASTIC_APM_VERSION}/elastic-apm-agent-${ELASTIC_APM_VERSION}.jar /usr/local/elastic-apm-agent.jar

# 4. Remove default Tomcat webapps (prevents accidental exposed admin pages)
RUN rm -rf /usr/local/tomcat/webapps/*

# 5. Copy unzipped directory as ROOT webapp
COPY ./server/target/oai-pmh-server/ ./ROOT/
