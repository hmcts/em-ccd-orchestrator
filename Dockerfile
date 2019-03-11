FROM hmcts/cnp-java-base:openjdk-8u191-jre-alpine3.9-2.0
MAINTAINER "HMCTS Team <https://github.com/hmcts>"
LABEL maintainer = "HMCTS Team <https://github.com/hmcts>"

WORKDIR /opt/app
COPY build/libs/rpa-em-ccd-orchestrator.jar .

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD http_proxy="" curl --silent --fail http://localhost:8080/health

EXPOSE 8080 5005

ENTRYPOINT exec java ${JAVA_OPTS} -jar "/opt/app/rpa-em-ccd-orchestrator.jar"
