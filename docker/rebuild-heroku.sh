#! /bin/bash

set -e

# setting up default values for flags
PUBLISH="false"

# reading flags
while getopts ":p" flag; do
    case "${flag}" in
    p)  PUBLISH=${OPTARG}
        ;;
    *)  echo "Unknown flag ${OPTARG}"
        exit 1
        ;;
    esac
done

# SCHEMA
echo "==================== rebuilding image ===================="
./build-and-publish.sh -e local -l true -c task-schema -p false

echo "==================== tagging image ===================="
docker tag azjarosz/task-schema:0.6.2-SNAPSHOT registry.heroku.com/task-test-be/schema

if [[ "$PUBLISH" = "true" ]]; then
    echo "==================== pushing image to docker registry ===================="
    docker push registry.heroku.com/task-test-be/schema

    echo "==================== releasing new version of the app ===================="
    heroku container:release schema -a task-test-be

    echo "==================== running one off worker ===================="
    heroku run schema --type=schema -a task-test-be
fi

# SAMPLE DATA
echo "==================== rebuilding image ===================="
./build-and-publish.sh -e local -l true -c task-sample-data -p false

echo "==================== tagging image ===================="
docker tag azjarosz/task-sample-data:0.6.2-SNAPSHOT registry.heroku.com/task-test-be/sample-data

if [[ "$PUBLISH" = "true" ]]; then
    echo "==================== pushing image to docker registry ===================="
    docker push registry.heroku.com/task-test-be/sample-data

    echo "==================== releasing new version of the app ===================="
    heroku container:release sample-data -a task-test-be

    echo "==================== running one off worker ===================="
    heroku run sample-data --type=sample-data -a task-test-be
fi

# BE
echo "==================== rebuilding image ===================="
./build-and-publish.sh -e local -l true -c task-backend -p false

echo "==================== tagging image ===================="
docker tag azjarosz/task-backend:0.6.2-SNAPSHOT registry.heroku.com/task-test-be/web

if [[ "$PUBLISH" = "true" ]]; then
    echo "==================== pushing image to docker registry ===================="
    docker push registry.heroku.com/task-test-be/web

    echo "==================== releasing new version of the app ===================="
    heroku container:release web -a task-test-be
fi
