#! /usr/bin/bash

HELP_TEXT="$(basename "$0") [-h help]

Script runs sonar scan on local environment. If sonar does not run it will try to make it run.
Scan runs against the current branch and will be available in sonar under PR-<branch-name>

"
SONAR_SERVER_URL="http://localhost:9000"
LOCAL_SONAR_TOKEN=squ_dc4af91f77966f97ee406554a427e2e4a58a94a5
SONAR_PATH_9=~/Desktop/Task/sonarqube-9.8.0.63668/bin/linux-x86-64/sonar.sh
SONAR_PATH_10=~/Desktop/Task/sonarqube-10.3.0.82913/bin/linux-x86-64/sonar.sh

while getopts h: flag; do
    case "${flag}" in
    h)
        echo "$HELP_TEXT"
        exit
        ;;
    esac
done

# check if service is running
SONAR_STATUS=$($SONAR_PATH_9 status | sed -n 2p)
if [[ $SONAR_STATUS = "SonarQube is not running." ]]; then
    SONAR_PATH_9 start
    sleep 10
fi

BRANCH=$(git name-rev --name-only HEAD)
echo $BRANCH
TASK_NUMBER=$(echo $BRANCH | sed 's/[^0-9]*//g')
echo $TASK_NUMBER
cd ~/Desktop/Task/Task/
mvn clean verify -T 1C
mvn sonar:sonar -Dsonar.projectKey=task -Dsonar.host.url=$SONAR_SERVER_URL -Dsonar.login=$LOCAL_SONAR_TOKEN -Dsonar.pullrequest.key=$TASK_NUMBER -Dsonar.pullrequest.branch=$BRANCH -Dsonar.pullrequest.base=develop
