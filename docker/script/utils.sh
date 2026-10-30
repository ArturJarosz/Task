#! /bin/bash

VERSION_PATTERN="^([0-9]|[1-9][0-9]*)\.([0-9]|[1-9][0-9]*)\.([0-9]|[1-9][0-9]*)(-(SNAPSHOT|((rc|beta|alpha)(\.[0-9]+){0,1}))){0,1}$"
BUILD_TYPES=("full" "only-update" "only-run" "pull-images")
MORE_INFORMATION="For more information about script parameters call: $0 -h"

displayHelpForCompose() {
    echo "Script to run environment."
    echo "Usage: $0 -e [environment] -v [version] -b [build type]"
    echo "Usage: $0 -h"
    echo "  -e      environment:"
    echo "          - local - running local environment and loading local.env file"
    echo "  -v      version:"
    echo "          - version of the application to run, like 1.0.0 or 0.5.0-SNAPSHOT"
    echo "  -b      build type:"
    echo "          - full - full redeploy of the application, with dropping tables and applying schema and then running application"
    echo "          - only-update - applying updating database schema without running the application"
    echo "          - only-run - starting database and application"
    echo "  -h      display this help"
    echo "Example: $0 local 0.6.2-SNAPSHOT full"
}

displayHelpForBuild() {
    echo "Script to build docker images."
    echo "Usage: $0 -e [environment] -l [true/false] -c [chosen module] -p [true/false] -l [true/false]"
    echo "Usage: $0 -C"
    echo "Usage: $0 -h"
    echo "Usage: $0 -a"
    echo "  -a      lists all available images that can be built using this script"
    echo "  -C      display current version of application"
    echo "  -e      environment:"
    echo "          - local - building images in the local environment "
    echo "          - github - building images in GitHub actions"
    echo "  -l      whether image should be tagged as latest"
    echo "  -c      chosen module to build image from, use -a to display all available options"
    echo "  -p      whether image should be published or not"
    echo "  -h      display this help."
    echo "Example: $0 -e local -l true -c task-schema -p true"
}

displayHelpForPull() {
        echo "Script to pull docker images."
        echo "Usage: $0 -e [environment] -v [version] -b [build type]"
        echo "Usage: $0 -h"
        echo "  -e      environment:"
        echo "          - local - running local environment and loading local.env file"
        echo "  -v      version:"
        echo "          - version of the application to run, like 1.0.0 or 0.5.0-SNAPSHOT"
        echo "  -b      build type:"
        echo "          - full - full redeploy of the application, with dropping tables and applying schema and then running application"
        echo "          - only-update - applying updating database schema without running the application"
        echo "          - run - starting database and application"
        echo "  -h      display this help"
        echo "Example: $0 local 0.6.2-SNAPSHOT full"
}

verifyMandatoryArgument() {
    if [[ -z "$1" ]]; then
        if [[ $HELP_DISPLAYED = false ]]; then
            echo "Use -h option to display help."
        fi
        exit 1
    fi
}

verifyVersion() {
    if ! [[ $1 =~ $VERSION_PATTERN ]]; then
        echo "Error: $1 is not valid version format."
        echo "Version number has to be in format: [NUMBER].[NUMBER].[NUMBER]-[QUALIFIER], where QUALIFIER is optional."
        echo "Qualifier is one of the fallowing: SNAPSHOT, rc, alpha, beta."
        echo "Examples of correct versions:"
        echo "0.1.0"
        echo "1.2.17"
        echo "2.3.0-SNAPSHOT"
        echo "1.12.1-rc"
        echo "0.1.0-alpha"
        echo "$MORE_INFORMATION"
        exit 1
    fi
}

verifyEnvironment() {
    envExtension=".env"
    foundEnv="false"
    currentEnvFile="$1$envExtension"
    files=( $(ls env) )
    for fileName in "${files[@]}"; do
        if [[ "$fileName" = "$currentEnvFile" ]]; then
            foundEnv="true"
        fi
    done
    if [[ "$foundEnv" = "false" ]]; then
        echo "$1 is not correct environment name. Could not find matching environment file: $currentEnvFile."
        echo "Available ones are: ${files[*]}"
        echo "$MORE_INFORMATION"
        exit 1
    fi
}

verifyBuildType() {
    foundType="false"
    for type in "${BUILD_TYPES[@]}"; do
        if [[ "$1" = "$type" ]]; then
            foundType="true"
        fi
    done
    if [[ "$foundType" = "false" ]]; then
        echo "$1 is not correct build type. It should be one of: ${BUILD_TYPES[*]}"
        echo "$MORE_INFORMATION"
        exit 1
    fi
}

verifyBoolean() {
    if ! [[ "$1" = "true" || "$1" = "false" ]]; then
        echo "Incorrect values. It has to be either 'true' or 'false'"
        echo "$MORE_INFORMATION"
        exit 1
    fi
}

notImplemented() {
    echo "This functionality [$1] is not implemented yet. Please run script again with different input."
    echo "$MORE_INFORMATION"
    exit 1
}

listServices() {
    echo "Services available for building:"
    docker compose --env-file "$ENV_FILE" -f docker-compose-full.yml config --services
}

verifyChosenModule() {
    availableServices=( $(docker compose --env-file "$ENV_FILE" -f docker-compose-full.yml config --services) )
    foundModule="false"
    for module in "${availableServices[@]}"; do
        if [[ "$1" = "${module}" ]]; then
            foundModule="true"
        fi
    done
    if [[ "${foundModule}" = "false" ]]; then
        echo "$1 is not correct module name."
        echo "Available ones are: ${availableServices[*]}"
        echo "$MORE_INFORMATION"
        exit 1
    fi
}

displayCurrentVersion() {
    currentVersion=$(mvn -f ../pom.xml help:evaluate -Dexpression=project.version -q -DforceStdout)
    echo "Current version is: ${currentVersion}"
}

waitForDatabase() {
    for _ in {1..30}; do
        if docker exec "$1" pg_isready -q -U "$DB_USER" > /dev/null 2>&1; then
            return 0
        fi
        sleep 2
    done
    return 1
}

# PostgreSQL 18+ images keep the cluster in taskDb/<major version>/docker, older ones kept it directly in taskDb.
# A cluster left there by an older image is moved to taskDb-pg<version>-backup. With keepData=true its database is
# copied into the new cluster first, so the environment starts with the data it had.
upgradeDatabaseCluster() {
    composeFile=$1
    keepData=$2
    image=$(docker compose --env-file "$ENV_FILE" -f "$composeFile" config --images task-database)
    oldVersion=$(docker run --rm -v "$(pwd)/taskDb:/data" --entrypoint sh "$image" -c \
        'cat /data/PG_VERSION 2>/dev/null || true')
    if [[ -z "$oldVersion" ]]; then
        return 0
    fi
    backupDir="taskDb-pg${oldVersion}-backup"
    dumpFile="taskDb-pg${oldVersion}.dump"
    newCluster=$(docker run --rm -v "$(pwd)/taskDb:/data" --entrypoint sh "$image" -c \
        'ls -d /data/*/docker 2>/dev/null || true')
    if [[ -n "$newCluster" ]]; then
        echo "Error: taskDb holds both a PostgreSQL ${oldVersion} cluster and a cluster of ${image} in ${newCluster#/data/}."
        echo "Remove taskDb/${newCluster#/data/} to have the PostgreSQL ${oldVersion} data moved into ${image}."
        exit 1
    fi
    if [[ -e "$backupDir" ]]; then
        echo "Error: taskDb holds a PostgreSQL ${oldVersion} cluster, but ${backupDir} to move it to already exists."
        exit 1
    fi

    echo "Found PostgreSQL ${oldVersion} cluster in taskDb, upgrading it to ${image}."
    if [[ $keepData = "true" ]]; then
        echo " -- Dumping database ${DB_DATABASE}"
        docker run -d --rm --name task-database-upgrade -v "$(pwd)/taskDb:/var/lib/postgresql/data" \
            "postgres:${oldVersion}" > /dev/null
        if ! waitForDatabase task-database-upgrade || ! docker exec task-database-upgrade \
                pg_dump -U "$DB_USER" -d "$DB_DATABASE" --format=custom > "$dumpFile"; then
            docker stop task-database-upgrade > /dev/null 2>&1 || true
            rm -f "$dumpFile"
            echo "Error: could not dump database ${DB_DATABASE}, taskDb was left as it was."
            exit 1
        fi
        docker stop task-database-upgrade > /dev/null
    fi
    docker run --rm -v "$(pwd):/work" --entrypoint mv "$image" /work/taskDb "/work/${backupDir}"
    echo " -- Moved PostgreSQL ${oldVersion} cluster to ${backupDir}, remove it once the upgraded database is checked"
    if [[ $keepData = "true" ]]; then
        echo " -- Restoring database ${DB_DATABASE}"
        docker compose --env-file "$ENV_FILE" -f "$composeFile" up -d --wait task-database
        if ! docker compose --env-file "$ENV_FILE" -f "$composeFile" exec -T task-database \
                pg_restore -U "$DB_USER" -d "$DB_DATABASE" --exit-on-error < "$dumpFile"; then
            echo "Error: could not restore ${dumpFile}, the PostgreSQL ${oldVersion} cluster is in ${backupDir}."
            exit 1
        fi
        rm "$dumpFile"
    fi
}

prepareNginxConf() {
    echo "Preparing nginx configuration"
    echo " -- Setting base url to $BASE_URL"
    export BASE_URL=$BASE_URL
    envsubst '$BASE_URL' < ./task-fe/nginx.conf > ./task-fe/default.conf
}

