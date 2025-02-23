#! /bin/bash

set -e

. ./utils.sh

ENVIRONMENT=""
export APP_VERSION=""
DEFAULT_ENV="github"
export ENV="${DEFAULT_ENV}"
# example file has to be set to pass eny information to compose file, to make it possible to list services in helper methods
export ENV_FILE="./env/example.env"

# reading script flags
while getopts "e:v:c:h" flag; do
    case "${flag}" in
        e)  ENVIRONMENT=${OPTARG}
            verifyEnvironment "$ENVIRONMENT"
            ;;
        v)  APP_VERSION=${OPTARG}
            ;;
        c)  CHOSEN_MODULE=${OPTARG}
            verifyChosenModule "$CHOSEN_MODULE"
            ;;
        h)  echo "display help"
            ;;
    esac
done

# checking correctness of inputs

if [[ -z "${APP_VERSION}" ]]; then
    APP_VERSION=$(mvn -f ../pom.xml help:evaluate -Dexpression=project.version -q -DforceStdout)
    # getting version gives unwanted '\x1b[0m' at the end of string, which needs to be removed
    APP_VERSION=$(echo "$APP_VERSION" | sed -r 's/\x1b\[[0-9;]*m?//g')
fi

export ENV="$ENVIRONMENT"
export ENV_FILE="./env/$ENVIRONMENT.env"
export APP_VERSION="$APP_VERSION"

echo "========================================================="
echo "Chosen module is: ${CHOSEN_MODULE}:${APP_VERSION}"
echo "Chosen environment: ${ENVIRONMENT}"
echo "========================================================="

echo "Pulling image..."
docker compose --env-file ${ENV_FILE} -f docker-compose-full.yml pull ${CHOSEN_MODULE}




