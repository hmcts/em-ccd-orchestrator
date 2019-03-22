FROM hmcts/cnp-java-base:openjdk-8u191-jre-alpine3.9-2.0.1

COPY build/libs/rpa-em-ccd-orchestrator.jar /opt/app/

CMD ["rpa-em-ccd-orchestrator.jar"]

EXPOSE 8080
