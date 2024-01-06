#! /bin/bash

set -e

. ./utils.sh

ENVIRONMENT=""
VERSION=""
BUILD_TYPE=""
HELP_DISPLAYED=false
COMPOSE_FILE=""

# reading script flags
while getopts "e:v:b:h" flag; do
    case "${flag}" in
        e)  ENVIRONMENT=${OPTARG}
            verifyEnvironment "$ENVIRONMENT"
            ;;
        v)  VERSION=${OPTARG}
            verifyVersion "$VERSION"
            ;;
        b)  BUILD_TYPE=${OPTARG}
            verifyBuildType "$BUILD_TYPE"
            ;;
        h)  HELP_DISPLAYED=true
            displayHelpForCompose
            ;;
        *)  displayHelpForCompose
            ;;
    esac
done

# checking correctness of inputs
verifyMandatoryArgument "$ENVIRONMENT" "Environment"
verifyMandatoryArgument "$VERSION" "Version"
verifyMandatoryArgument "$BUILD_TYPE" "Build type"

# exporting environment variables
export ENV="$ENVIRONMENT"
export ENV_FILE="./env/$ENVIRONMENT.env"
export APP_VERSION="$VERSION"
export BUILD_TYPE="$BUILD_TYPE"

# triggering composing
case "$BUILD_TYPE" in
    "full") echo "full deployment..."
        COMPOSE_FILE="docker-compose-full.yml"
        ;;
    "only-update") echo "only update deployment"
        # TODO: TA-407 create configuration for only updating schema
        notImplemented "$BUILD_TYPE"
        exit 1
        ;;
    "only-run") echo "only run deployment"
        COMPOSE_FILE="docker-compose-run.yml"
        ;;
esac

docker compose --env-file "$ENV_FILE" -f "docker-compose-full.yml" down --remove-orphans
if [[ "local" != "${ENV}" ]]; then
    docker compose --env-file "$ENV_FILE" -f "${COMPOSE_FILE}" pull
fi
docker compose --env-file "$ENV_FILE" -f "${COMPOSE_FILE}" up -d
