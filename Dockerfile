FROM openjdk:11-jdk-slim

ARG JAR_FILE=census-rm-action-processor*.jar
CMD ["/usr/local/openjdk-11/bin/java", "-jar", "/opt/census-rm-action-processor.jar"]

COPY healthcheck.sh /opt/healthcheck.sh
RUN chmod +x /opt/healthcheck.sh

RUN groupadd --gid 999 actionprocessor && \
    useradd --create-home --system --uid 999 --gid actionprocessor actionprocessor
USER actionprocessor

COPY target/$JAR_FILE /opt/census-rm-action-processor.jar
