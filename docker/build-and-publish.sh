#! /bin/bash

set -e

. ./utils.sh

DEFAULT_ENV="github"
CONTINUE="true"
LATEST=""
PUBLISH=""
ORGANIZATION="azjarosz"

export ENV="${DEFAULT_ENV}"
# example file has to be set to pass eny information to compose file, to make it possible to list services in helper methods
export ENV_FILE="./env/example.env"
export APP_VERSION=""
export BUILD_TYPE="full"

# reading script flags
while getopts "aCe:l:c:p:h" flag; do
    case "${flag}" in
        a)  docker compose --env-file "$ENV_FILE" -f docker-compose-full.yml config --services
            CONTINUE="false"
            ;;
        C)  displayCurrentVersion
            ;;
        h)  displayHelpForBuild
            CONTINUE="false"
            ;;
        e)  ENVIRONMENT=${OPTARG}
            verifyEnvironment "$ENVIRONMENT"
            ;;
        l)  LATEST=${OPTARG}
            verifyBoolean "${LATEST}"
            ;;
        c)  CHOSEN_MODULE=${OPTARG}
            verifyChosenModule "${CHOSEN_MODULE}"
            ;;
        p)  PUBLISH=${OPTARG}
            verifyBoolean "${PUBLISH}"
            ;;
        *)  displayHelpForBuild
            CONTINUE="false"
            ;;
    esac
done

if [[ "$CONTINUE" = "false" ]]; then
    exit 1
fi

verifyMandatoryArgument "$ENVIRONMENT" "Environment"
verifyMandatoryArgument "$CHOSEN_MODULE" "Module"
verifyMandatoryArgument "$LATEST" "Latest"

echo "Environment set to $ENVIRONMENT"
export ENV_FILE="./env/$ENVIRONMENT.env"

# getting version from main pom.xml
APP_VERSION=$(mvn -f ../pom.xml help:evaluate -Dexpression=project.version -q -DforceStdout)
# getting version gives unwanted '\x1b[0m' at the end of string, which needs to be removed
APP_VERSION=$(echo "$APP_VERSION" | sed -r 's/\x1b\[[0-9;]*m?//g')

source "${ENV_FILE}"

# build image
echo "Building ${CHOSEN_MODULE}:${APP_VERSION} image."
docker compose --env-file "$ENV_FILE" -f docker-compose-full.yml build --no-cache --progress plain "${CHOSEN_MODULE}"
# checking build result
buildResult=$?
# if success - publish, if not return error message
if [[ "${buildResult}" != "0" ]]; then
    echo "Building image was not successful. Cannot publish."
    exit 1
fi

if [[ "${LATEST}" = "true" ]]; then
    echo "Tagging image as ${ORGANIZATION}/${CHOSEN_MODULE}:latest."
    docker tag ${ORGANIZATION}/${CHOSEN_MODULE}:${APP_VERSION} ${ORGANIZATION}/${CHOSEN_MODULE}:latest
fi

# publishing image
if [[ "${PUBLISH}" = "true" ]]; then
    echo "Publishing image."
    docker image push --all-tags ${ORGANIZATION}/${CHOSEN_MODULE}
fi
