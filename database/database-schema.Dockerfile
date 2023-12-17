FROM maven:3.9.2-eclipse-temurin-17 as build

RUN mkdir /task-schema-build
COPY . /task-schema-build
COPY ./docker/settings.xml /root/.m2/settings.xml

WORKDIR /task-schema-build

RUN mvn clean package -DskipTests -P fatJar -X

FROM eclipse-temurin:17-jre as task-base

RUN mkdir /task-schema-app
RUN groupadd -r task && useradd --no-log-init -r -g task task

COPY --from=build /task-schema-build/target/task-database-fat.jar /task-schema-app/task-database-fat.jar

RUN chown -R task:task /task-schema-app
WORKDIR /task-schema-app

RUN chmod uo+x task-database-fat.jar

USER task

RUN ls -l
RUN java --version

ENTRYPOINT ["java", \
            "-jar", \
            "task-database-fat.jar", \
            "--spring.profiles.active=production",  \
            "--spring.datasource.username=${DB_USER}", \
            "--spring.datasource.url=jdbc:postgresql://task-database:5432/${DB_DATABASE}", \
            "--spring.datasource.password=${DB_PASSWORD}", \
            "--spring.datasource.driver-class-name=org.postgresql.Driver", \
            "--spring.liquibase.drop-first=true", \
            "--file.encoding=UTF-8"]
