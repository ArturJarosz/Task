FROM maven:3.9.2-eclipse-temurin-17 as build

RUN mkdir /task-sample-data-build
COPY . /task-sample-data-build
COPY ./docker/settings.xml /root/.m2/settings.xml

WORKDIR /task-sample-data-build

RUN mvn clean package -Dmaven.test.skip=true -P fatJar

FROM eclipse-temurin:17-jre as sample-data-base

RUN mkdir /task-sample-data-app
RUN groupadd -r task && useradd --no-log-init -r -g task task

COPY --from=build /task-sample-data-build/target/task-sample-data-fat.jar /task-sample-data-app/task-sample-data-fat.jar

RUN chown -R task:task /task-sample-data-app
WORKDIR /task-sample-data-app

RUN chmod uo+x task-sample-data-fat.jar

USER task

ENTRYPOINT ["java", \
            "-jar", \
            "task-sample-data-fat.jar", \
            "--spring.datasource.username=${DB_USER}", \
            "--spring.datasource.url=jdbc:postgresql://task-database:5432/${DB_DATABASE}", \
            "--spring.datasource.password=${DB_PASSWORD}", \
            "--spring.datasource.driver-class-name=org.postgresql.Driver"]
