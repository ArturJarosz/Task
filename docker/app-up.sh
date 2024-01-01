#! /usr/bin/bash

set -e

. ./utils.sh

ENVIRONMENT=""
VERSION=""
BUILD_TYPE=""
HELP_DISPLAYED=false

# reading script flags
while getopts "e:v:b:h" flag
do
    case "${flag}" in
        e) ENVIRONMENT=${OPTARG}
            checkEnvironment "$ENVIRONMENT"
            ;;
        v) VERSION=${OPTARG}
            checkVersion "$VERSION"
            ;;
        b) BUILD_TYPE=${OPTARG}
            checkBuildType "$BUILD_TYPE"
            ;;
        h) HELP_DISPLAYED=true
            displayHelp
            ;;
    esac
done

# checking correctness of inputs
checkMandatoryArgument "$ENVIRONMENT" "Environment"
checkMandatoryArgument "$VERSION" "Version"
checkMandatoryArgument "$BUILD_TYPE" "Build type"

# exporting environment variables
export ENV="$ENVIRONMENT"
export ENV_FILE="./env/$ENVIRONMENT.env"
export APP_VERSION="$VERSION"
export BUILD_TYPE="$BUILD_TYPE"

# triggering composing
case "$BUILD_TYPE" in
    "full") echo "full deployment..."
        docker compose --env-file "$ENV_FILE" -f docker-compose-full.yml up -d
        ;;
    "only-update") echo "only update deployment"
        # TODO: TA-407 create configuration for only updating schema
        notImplemented "$BUILD_TYPE"
        exit 1
        ;;
    "only-run") echo "only run deployment"
    docker compose --env-file "$ENV_FILE" up -d task-database task-backend
        ;;
esac
#
#echo "Env file is : ${ENV_FILE}"
#export $(xargs < "$ENV_FILE")

#docker compose --env-file "$ENV_FILE" build --no-cache --progress plain
#docker compose --env-file "$ENV_FILE" build
