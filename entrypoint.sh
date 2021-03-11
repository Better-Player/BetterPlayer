#!/bin/bash

# Docker Entrypoint script
# This script will perform checks on environmental variables
# If all checks pass, it will start BetterPlayer

RED='\033[0;31m'
GREEN='\033[0;32m'
ORANGE='\033[0;33m'
NC='\033[0m' # No Color

var_not_set() {
    printf "${RED}FAIL${NC}\n"
    printf "Environmental variable ${ORANGE}${1}${NC} not set. Cannot start BetterPlayer!\n"
    exit 1    
}

printf "Running preflight checks..."

if [[ -z "$BOT_TOKEN" ]]
then
    var_not_set "BOT_TOKEN"
fi

if [[ -z "$USE_GOOGLE_API" ]]
then
    var_not_set "USE_GOOGLE_API"
fi

if [[ -z "$GOOGLE_API_KEY" ]]
then
    var_not_set "GOOGLE_API_KEY"
fi

if [[ -z "$DB_HOST" ]]
then
    var_not_set "DB_HOST"
fi

if [[ -z "$DB_NAME" ]]
then
    var_not_set "DB_NAME"
fi

if [[ -z "$DB_USERNAME" ]]
then
    var_not_set "DB_USERNAME"
fi

if [[ -z "$DB_PASSWORD" ]]
then
    var_not_set "DB_PASSWORD"
fi

printf "${GREEN}OK${NC}\n" 
echo "Starting betterplayer!"

java -jar /app/betterplayer.jar