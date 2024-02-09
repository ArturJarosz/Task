# Task application

## 1. Last action statuses

Building and running tests on last PR or commit

[![Workflow without publishing](https://github.com/ArturJarosz/Task/actions/workflows/build%20and%20test.yml/badge.svg)](https://github.com/ArturJarosz/Task/actions/workflows/build%20and%20test.yml)

Building, running tests, building maven artifacts and publishing to artifactory

[![Publish artifact](https://github.com/ArturJarosz/Task/actions/workflows/publish.yml/badge.svg)](https://github.com/ArturJarosz/Task/actions/workflows/publish.yml)

## 2. Running application for development

### 2.1. Docker workflow

To make full environment running, go to the `docker` directory:

```bash
cd docker
```

and then lunch docker compose script with needed flags, like this:

```bash
./app-up.sh -e local -v 0.6.2-SNAPSHOT -b full
```

For more help with script run with `-h` (help) flag:

```bash 
./app-up.sh -h
```

That script uses matching `.env` with passed environment (for `local` it will be `local.env`) located in /docker/env.
To make it work provide all needed environment variables there. You can uses `example.env` for full list of them.

### 2.2. Local environment without docker (with IntelliJ)

#### Prerequisites

Project requires JDK in version 17, Maven, and instance of Postgresql database.

Clone current repository locally:

```bash 
git clone git@github.com:ArturJarosz/Task.git
```

Import whole project to your IDE. With the project there should be 4 templates to run SpringBoot application imported.
Edit them to fill needed information like password, user and database and run them in order:

1. `Drop and Init DB`
2. `Load sample data` (optional if you need some data to test application).
3. `Run Task BE`

If you have running version of the database with older version of application, you may want to run `Update DB`.

Please be aware that these run configurations use `local.env` file as well. So it might be a need that that file on your
system has to be prepared from scratch. You can use `/docker/env/example.env` as a template.

### 2.3. Running frontend part

To run FE part of the application please follow instruction in corresponding
repository: `https://github.com/ArturJarosz/Task-FE`.
