#!/bin/bash

set -e

IDAM_URI=$1
USERNAME=$2
PASSWORD=$3

curl --silent --header 'Content-Type: application/x-www-form-urlencoded' --header 'Accept: application/json' -d "username=${USERNAME}&password=${PASSWORD}" "${IDAM_URI}/loginUser" | jq -r .api_auth_token
