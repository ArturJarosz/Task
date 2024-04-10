#! /bin/bash

VERSION_PATTERN="^([0-9]|[1-9][0-9]*)\.([0-9]|[1-9][0-9]*)\.([0-9]|[1-9][0-9]*)(-(SNAPSHOT|((rc|beta|alpha)(\.[0-9]+){0,1}))){0,1}$"
BUILD_TYPES=("full" "only-update" "only-run")
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

