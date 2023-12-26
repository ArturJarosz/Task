#! /usr/bin/bash

set -e

HELP_TEXT="$(basename "$0") [-h help] [-n new version] [-f future version]

Script changes version of project to new version, creates branch for release with that version, and then changes version used in develop branch to future version.

Options:
  -h - show this help
  -n - new version that should be used in release
  -f - future version, that should be used after release

Version of product should be specified in semantic versioning approach - major.minor.path with eventual suffixes like rc1.
Examples: 0.5.5, 1.3.5-rc1, 2.0.0.
"

# reading versions from input
while getopts n:f:h flag; do
    # shellcheck disable=SC2220
    case "${flag}" in
    n)
        NEW_VERSION=${OPTARG}
        echo "$NEW_VERSION"
        if ! [[ $NEW_VERSION =~ ^([0-9]|[1-9][0-9]*)\.([0-9]|[1-9][0-9]*)\.([0-9]|[1-9][0-9]*)(-(SNAPSHOT|((rc|beta|alpha)(\.[0-9]+){0,1}))){0,1}$ ]]; then
            echo "Error: $NEW_VERSION is not valid version format. See help (-h) for more details."
            exit 1
        fi
        ;;
    f)
        FUTURE_VERSION=${OPTARG}
        echo "$FUTURE_VERSION"
        if ! [[ $FUTURE_VERSION =~ ^([0-9]|[1-9][0-9]*)\.([0-9]|[1-9][0-9]*)\.([0-9]|[1-9][0-9]*)(-(SNAPSHOT|((rc|beta|alpha)(\.[0-9]+){0,1}))){0,1}$ ]]; then
            echo "Error: $FUTURE_VERSION is not valid version format. See help (-h) for more details."
            exit 1
        fi
        ;;
    h)
        echo "$HELP_TEXT"
        exit
        ;;
    esac
done

if [[ -n "$NEW_VERSION" && -n "$FUTURE_VERSION" ]]; then
    cd ..
    echo "Using $NEW_VERSION for release"
    echo "Using $FUTURE_VERSION for future develop"

    echo "1. Creating release branch - release_$NEW_VERSION"
    git checkout -b release/release_"$NEW_VERSION"

    echo "2. Changing version in poms for release to $NEW_VERSION"
    mvn versions:set -DnewVersion=$NEW_VERSION -DgenerateBackupPoms=false -f pom.xml

    echo "3. Committing new version $NEW_VERSION to release branch"
    git add ":**\pom.xml"
    git commit -m "Changing version to $NEW_VERSION"

    echo "4. Creating new git tag: v$NEW_VERSION"
    git tag v$NEW_VERSION

    echo "5. Changing version on develop to $FUTURE_VERSION-SNAPSHOT"
    git checkout develop
    git checkout -b update/NO_JIRA_update_develop_to_$FUTURE_VERSION
    mvn versions:set -DnewVersion=$FUTURE_VERSION-SNAPSHOT -DgenerateBackupPoms=false -f pom.xml
    git add ":**\pom.xml"

    echo "6. Committing new version $FUTURE_VERSION-SNAPSHOT"
    git commit -m "Changing version to $FUTURE_VERSION-SNAPSHOT"

    echo "Furthers steps that need to be taken."
    echo "Because of possible conflicts no auto merging is being done."
    echo "Please merge release/release_$NEW_VERSION" to main branch.
    echo "Please merge update/NO_JIRA_update_develop_to_$FUTURE_VERSION to develop."
fi

