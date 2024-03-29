ARG APP_INSIGHTS_AGENT_VERSION=3.4.18

FROM hmctspublic.azurecr.io/base/java:21-distroless

COPY lib/applicationinsights.json /opt/app/
COPY build/libs/rpa-em-ccd-orchestrator.jar /opt/app/


CMD ["rpa-em-ccd-orchestrator.jar"]

EXPOSE 8080
