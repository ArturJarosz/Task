FROM maven:3.8.7-eclipse-temurin-17-alpine as build

ARG GITHUB_MAVEN_TOKEN

RUN mkdir /core-build
COPY . /core-build
COPY ./docker/settings.xml /root/.m2/settings.xml

WORKDIR /core-build

RUN mvn clean package -P fatJar -Dmaven.test.skip=true

FROM eclipse-temurin:17-jre-alpine as core-base

RUN mkdir /core-app
RUN addgroup task && adduser --disabled-password task --ingroup task

COPY --from=build /core-build/target/task-core-fat.jar /core-app/task-core-fat.jar

RUN chown -R task:task /core-app
WORKDIR /core-app

RUN chmod uo+x task-core-fat.jar

COPY --chown=task:task entrypoint.sh .
RUN chmod +x entrypoint.sh

USER task

CMD "./entrypoint.sh"
