FROM maven:3.8.7-eclipse-temurin-17-alpine as build

ARG GITHUB_MAVEN_TOKEN

RUN mkdir /task-sample-data-build
COPY . /task-sample-data-build
COPY ./docker/settings.xml /root/.m2/settings.xml

WORKDIR /task-sample-data-build

RUN mvn clean package -Dmaven.test.skip=true -P fatJar

FROM eclipse-temurin:17-jre-alpine as sample-data-base

RUN mkdir /task-sample-data-app
RUN addgroup task && adduser --disabled-password task --ingroup task

COPY --from=build /task-sample-data-build/target/task-sample-data-fat.jar /task-sample-data-app/task-sample-data-fat.jar

RUN chown -R task:task /task-sample-data-app
WORKDIR /task-sample-data-app

RUN chmod uo+x task-sample-data-fat.jar

COPY --chown=task:task entrypoint.sh .
RUN chmod +x entrypoint.sh

USER task

CMD "./entrypoint.sh"
