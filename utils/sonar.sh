#! /usr/bin/bash

HELP_TEXT="$(basename "$0") [-h help]

Script runs sonar scan on local environment. If sonar does not run it will try to make it run.
Scan runs against the current branch and will be available in sonar under PR-<branch-name>

"
SONAR_SERVER_URL="http://localhost:9000"
LOCAL_SONAR_TOKEN=sqa_495f7dd5ae43f79b742b1ddb00274fc7d339e091
SONAR_PATH_9=~/Desktop/Task/sonarqube-9.8.0.63668/bin/linux-x86-64/sonar.sh
SONAR_PATH_10=~/Desktop/Task/sonarqube-10.4.1.88267/bin/linux-x86-64/sonar.sh

while getopts h: flag; do
    case "${flag}" in
    h)
        echo "$HELP_TEXT"
        exit
        ;;
    esac
done

# check if service is running
SONAR_STATUS=$($SONAR_PATH_10 status | sed -n 2p)
if [[ $SONAR_STATUS = "SonarQube is not running." ]]; then
    $SONAR_PATH_10 start
    sleep 10
fi

BRANCH=$(git name-rev --name-only HEAD)
echo $BRANCH
TASK_NUMBER=$(echo $BRANCH | sed 's/[^0-9]*//g')
echo $TASK_NUMBER
cd ~/Desktop/Task/Task/
mvn clean verify -T 1C
mvn sonar:sonar -Dsonar.projectKey=Task-BE -Dsonar.host.url=$SONAR_SERVER_URL -Dsonar.token=$LOCAL_SONAR_TOKEN -Dsonar.pullrequest.key=$TASK_NUMBER -Dsonar.pullrequest.branch=$BRANCH -Dsonar.pullrequest.base=develop
# check main branch
# mvn sonar:sonar -Dsonar.projectKey=Task-BE -Dsonar.host.url=http://localhost:9000 -Dsonar.token=sqa_495f7dd5ae43f79b742b1ddb00274fc7d339e091
