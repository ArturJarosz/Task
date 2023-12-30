FROM maven:3.8.7-eclipse-temurin-17 as build

RUN mkdir /core-build
COPY . /core-build
COPY ./docker/settings.xml /root/.m2/settings.xml

WORKDIR /core-build

RUN mvn clean package -P fatJar -Dmaven.test.skip=true

FROM eclipse-temurin:17-jre as coe-base

RUN mkdir /core-app
RUN groupadd -r task && useradd --no-log-init -r -g task task

COPY --from=build /core-build/target/task-core-fat.jar /core-app/task-core-fat.jar

RUN chown -R task:task /core-app
WORKDIR /core-app

RUN chmod uo+x task-core-fat.jar

USER task

ENTRYPOINT ["java", \
            "-jar", \
            "task-core-fat.jar", \
            "--spring.datasource.username=${DB_USER}", \
            "--spring.datasource.url=jdbc:postgresql://task-database:5432/${DB_DATABASE}", \
            "--spring.datasource.password=${DB_PASSWORD}", \
            "--spring.datasource.driver-class-name=org.postgresql.Driver"]
