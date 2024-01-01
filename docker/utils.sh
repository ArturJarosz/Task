#! /usr/bin/bash

VERSION_PATTERN="^([0-9]|[1-9][0-9]*)\.([0-9]|[1-9][0-9]*)\.([0-9]|[1-9][0-9]*)(-(SNAPSHOT|((rc|beta|alpha)(\.[0-9]+){0,1}))){0,1}$"
BUILD_TYPES=("full" "only-update" "only-run")
MORE_INFORMATION="For more information about script parameters call: $0 -h"


displayHelp() {
    echo "Script to run environment."
    echo "Usage: $0 -e [environment] -v [version] -b [build type] -h [help]"
    echo "  -e      environment:"
    echo "          - local - running local environment and loading local.env file"
    echo "  -v      version:"
    echo "          - version of the application to run, like 1.0.0 or 0.5.0-SNAPSHOT"
    echo "  -b      build type:"
    echo "          - full - full redeploy of the application, with dropping tables and applying schema and then running application"
    echo "          - only-update - applying updating database schema without running the application"
    echo "          - run - starting database and application"
    echo "  Example: $0 local 0.6.2-SNAPSHOT full"
}

checkMandatoryArgument() {
    if [[ -z "$1" ]]; then
        if [[ $HELP_DISPLAYED = false ]]; then
            echo "Use -h option to display help."
        fi
        exit 1
    fi
}

checkVersion() {
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

checkEnvironment() {
    envExtension=".env"
    foundEnv=false
    currentEnvFile="$1$envExtension"
    files=( $(ls env) )
    for fileName in ${files[@]}; do
        if [[ "$fileName" = "$currentEnvFile" ]]; then
            foundEnv=true
        fi
    done
    if [[ "$foundEnv" = false ]]; then
        echo "Wrong environment set. Could not find matching environment file: $currentEnvFile ."
        exit 1
    fi
}

checkBuildType() {
    foundType=false
    for type in ${BUILD_TYPES[@]}; do
        if [[ "$1" = "$type" ]]; then
            foundType=true
        fi
    done
    if [[ "$foundType" = false ]]; then
        echo "$1 is not correct build type. It should be one of: ${BUILD_TYPES[*]}"
        exit 1
    fi
}

moreInformation () {
    echo
}

notImplemented() {
    echo "This functionality [$1] is not implemented yet. Please run script again with different input."
    echo "$MORE_INFORMATION"
    exit 1
}
