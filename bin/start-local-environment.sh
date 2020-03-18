#!/bin/bash

# Set variables
COMPOSE_FILE="-f docker-compose-dependencies.yml"
IDAM_URI="http://localhost:5000"
IDAM_USERNAME="idamOwner@hmcts.net"
IDAM_PASSWORD="Ref0rmIsFun"
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
      token=$(./bin/idam-authenticate.sh {IDAM_URI} {IDAM_USERNAME} {IDAM_PASSWORD})
done

echo "Setting up IDAM client..."
#Create a ccd gateway client
curl -XPOST \
  ${IDAM_URI}/services \
 -H "Authorization: AdminApiAuthToken ${token}" \
 -H "Content-Type: application/json" \
 -d '{"description": "em", "label": "em", "oauth2ClientId": "webshow", "oauth2ClientSecret": "AAAAAAAAAAAAAAAA", "oauth2RedirectUris": ["http://localhost:8080/oauth2redirect"], "selfRegistrationAllowed": true}'

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

