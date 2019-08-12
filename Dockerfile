FROM hmctspublic.azurecr.io/base/java:openjdk-8-distroless-1.0

COPY build/libs/rpa-em-ccd-orchestrator.jar /opt/app/

CMD ["rpa-em-ccd-orchestrator.jar"]

EXPOSE 8080
