#!/bin/bash

# Set variables
COMPOSE_FILE="-f docker-compose-dependencies.yml"
export DOCMOSIS_ACCESS_KEY=$1

echo "Starting shared-db..."
docker-compose ${COMPOSE_FILE} up -d shared-db

echo "Starting IDAM(ForgeRock)..."
docker-compose ${COMPOSE_FILE} up -d fr-am
docker-compose ${COMPOSE_FILE} up -d fr-idm


echo "Starting IDAM..."
docker-compose ${COMPOSE_FILE} up -d idam-api \
                                     idam-web-public \
                                     idam-web-admin
echo "Testing IDAM Authentication..."
token=$(./bin/idam-authenticate.sh http://localhost:5000 idamowner@hmcts.net Ref0rmIsFun)
while [ "_${token}" = "_" ]; do
      sleep 60
      echo "idam-api is not running! Check logs, you may need to restart"
      token=$(./bin/idam-authenticate.sh http://localhost:5000 idamowner@hmcts.net Ref0rmIsFun)
done

echo "Starting dependencies..."
docker-compose ${COMPOSE_FILE} build
docker-compose ${COMPOSE_FILE} up -d shared-database \
                                     service-auth-provider-api \
                                     azure-storage-emulator-azurite \
                                     make-container-call \
                                     dm-store \
                                     ccd-user-profile-api \
                                     ccd-definition-store-api \
                                     ccd-data-store-api \
                                     ccd-api-gateway \
                                     ccd-case-management-web \
                                     smtp-server \
                                     rpa-em-ccd-orchestrator \
                                     stitching-api

echo "LOCAL ENVIRONMENT SUCCESSFULLY STARTED"

