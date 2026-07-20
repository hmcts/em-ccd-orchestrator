ARG APP_INSIGHTS_AGENT_VERSION=3.4.18

FROM hmctsprod.azurecr.io/base/java:25-distroless

USER hmcts
COPY lib/applicationinsights.json /opt/app/
COPY build/libs/rpa-em-ccd-orchestrator.jar /opt/app/


CMD ["rpa-em-ccd-orchestrator.jar"]

EXPOSE 8080
