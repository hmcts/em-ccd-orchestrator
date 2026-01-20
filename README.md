# EVIDENCE MANAGEMENT CCD Orchestrator

[![Build Status](https://travis-ci.org/hmcts/rpa-em-ccd-orchestrator.svg?branch=master)](https://travis-ci.org/hmcts/rpa-em-ccd-orchestrator)
[![codecov](https://codecov.io/gh/hmcts/rpa-em-ccd-orchestrator/branch/master/graph/badge.svg)](https://codecov.io/gh/hmcts/rpa-em-ccd-orchestrator)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

CCD Orchestrator is a backend service that facilitates interactions between CCD, the EM Stitching service, and a calling service.


# Setup
Install `https://stedolan.github.io/jq/`

For linux: `sudo apt-get install jq`

For mac: `brew install jq`

#### To clone repo and prepare to pull containers:
```
git clone https://github.com/hmcts/em-ccd-orchestrator.git
cd em-ccd-orchestrator
```

#### Clean and build the application:
```
./gradlew clean
./gradlew build
```

#### To run the application:

VPN connection is required

```
./gradlew bootRun
```

To run functional tests, em-stitching-api must also be running.
Please follow the instructions in the README for em-stitching-api on how to do so.


This will start the API container exposing the application's port
(set to `8080` in this template app).

In order to test if the application is up, you can call its health endpoint:

```
  curl http://localhost:8080/health
```

You should get a response similar to this:

```
  {"status":"UP","components":{"discoveryComposite":{"description":"Discovery Client not initialized","status":"UNKNOWN",
"components":{"discoveryClient":{"description":"Discovery Client not initialized","status":"UNKNOWN"}}},
"ping":{"status":"UP"},"refreshScope":{"status":"UP"},"serviceAuth":{"status":"UP"}}}
```


### Tech

It uses:

* Java 11
* Spring boot
* Junit, Mockito and SpringBootTest and Powermockito
* Gradle
* [lombok project](https://projectlombok.org/) - Lombok project

### Plugins
* [lombok plugin](https://plugins.jetbrains.com/idea/plugin/6317-lombok-plugin) - Lombok IDEA plugin

### Swagger UI
To view our REST API go to http://{HOST}/swagger-ui/index.html
On local machine with server up and running, link to swagger is as below
> http://localhost:8080/swagger-ui/index.html
> if running on AAT, replace localhost with ingressHost data inside values.yaml class in the necessary component, making sure port number is also removed.

### API Endpoints
A list of our endpoints can be found here
> https://hmcts.github.io/cnp-api-docs/swagger.html?url=https://hmcts.github.io/cnp-api-docs/specs/em-ccd-orchestrator.json

### Automated Bundling Configuration Validation

The bundle configuration files can be validated by executing the `validateYaml` task:

```
./gradlew validateYaml
```


## Plugins

The template contains the following plugins:

  * checkstyle

    https://docs.gradle.org/current/userguide/checkstyle_plugin.html

    Performs code style checks on Java source files using Checkstyle and generates reports from these checks.
    The checks are included in gradle's *check* task (you can run them by executing `./gradlew check` command).



  * jacoco

    https://docs.gradle.org/current/userguide/jacoco_plugin.html

    Provides code coverage metrics for Java code via integration with JaCoCo.
    You can create the report by running the following command:

    ```bash
      ./gradlew jacocoTestReport
    ```

    The report will be created in build/reports subdirectory in your project directory.

  * io.spring.dependency-management

    https://github.com/spring-gradle-plugins/dependency-management-plugin

    Provides Maven-like dependency management. Allows you to declare dependency management
    using `dependency 'groupId:artifactId:version'`
    or `dependency group:'group', name:'name', version:version'`.

  * org.springframework.boot

    http://projects.spring.io/spring-boot/

    Reduces the amount of work needed to create a Spring application

  * org.owasp.dependencycheck

    https://jeremylong.github.io/DependencyCheck/dependency-check-gradle/index.html

    Provides monitoring of the project's dependent libraries and creating a report
    of known vulnerable components that are included in the build. To run it
    execute `gradle dependencyCheck` command.

  * com.github.ben-manes.versions

    https://github.com/ben-manes/gradle-versions-plugin

    Provides a task to determine which dependencies have updates. Usage:

    ```bash
      ./gradlew dependencyUpdates -Drevision=release
    ```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details

### Check the build

### Running contract or pact tests:

You can run contract or pact tests as follows:
```
./gradlew clean
```

```
./gradlew contract
```

You can then publish your pact tests locally by first running the pact docker-compose:

```
docker-compose -f docker-pactbroker-compose.yml up
```

and then using it to publish your tests:

```
./gradlew pactPublish
```
