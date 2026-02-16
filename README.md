# EVIDENCE MANAGEMENT CCD Orchestrator

[![codecov](https://codecov.io/gh/hmcts/em-ccd-orchestrator/branch/master/graph/badge.svg)](https://codecov.io/gh/hmcts/em-ccd-orchestrator)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

CCD Orchestrator is a backend service that facilitates interactions between CCD, the EM Stitching service, and a calling service.

## Prerequisites

Before setting up the project, ensure you have the following installed:

- **Java 21** - Required for building and running the application
- **jq** - JSON processor for command-line
  - Linux: `sudo apt-get install jq`
  - Mac: `brew install jq`
- **Azure CLI** - Required for loading environment secrets from Azure Key Vault
  - Install: `brew install azure-cli` (Mac) or follow [Azure CLI installation guide](https://docs.microsoft.com/en-us/cli/azure/install-azure-cli)
- **VPN Connection** - Required for running the application locally and accessing HMCTS infrastructure
- **Docker** (optional) - For running containerized services

# Setup

#### To clone repo and prepare to pull containers:
```bash
git clone https://github.com/hmcts/em-ccd-orchestrator.git
cd em-ccd-orchestrator
```

#### Clean and build the application:
```bash
./gradlew clean
./gradlew build
```

#### To run the application:

**Important**: VPN connection is required

em-stitching-api must also be running.
Please follow the instructions in the README for em-stitching-api on how to do so.


The application will automatically load environment secrets from Azure Key Vault (requires Azure CLI authentication):

```bash
./gradlew bootRun
```

#### Environment Configuration

The application uses environment variables stored in Azure Key Vault. When running `bootRun`, the `loadEnvSecrets` task automatically:
1. Connects to Azure Key Vault (`em-ccdorc-aat`)
2. Downloads the secrets
3. Creates a `.aat-env` file locally
4. Loads the variables into the application

To manually authenticate with Azure:
```bash
az login
```


This will start the API container exposing the application's port
(set to `8080` in this template app).

In order to test if the application is up, you can call its health endpoint:

```bash
curl http://localhost:8080/health
```

You should get a response similar to this:

```json
{
  "status": "UP",
  "components": {
    "discoveryComposite": {
      "description": "Discovery Client not initialized",
      "status": "UNKNOWN",
      "components": {
        "discoveryClient": {
          "description": "Discovery Client not initialized",
          "status": "UNKNOWN"
        }
      }
    },
    "ping": {"status": "UP"},
    "refreshScope": {"status": "UP"},
    "serviceAuth": {"status": "UP"}
  }
}
```


### Tech

It uses:

* Java 21
* Spring Boot 3.5.10
* JUnit 5 (Jupiter), Mockito, and Spring Boot Test
* Gradle
* [Lombok](https://projectlombok.org/) - Reduces boilerplate code
* Serenity BDD - For functional testing
* Pact - For contract testing

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

```bash
./gradlew validateYaml
```

## Testing

The project includes multiple types of tests:

### Unit Tests
Run standard unit tests:
```bash
./gradlew test
```

### Integration Tests
Run integration tests:
```bash
./gradlew integration
```

### Functional Tests
Run functional/acceptance tests (requires VPN and `.aat-env` configuration):
```bash
./gradlew functional
```

**Note**: To run functional tests, `em-stitching-api` must also be running. Please follow the instructions in the em-stitching-api README.

### Smoke Tests
Run non-destructive smoke tests:
```bash
./gradlew smoke
```

### Contract Tests (Pact)

#### Consumer Contract Tests
Run consumer pact tests:
```bash
./gradlew contract
```

#### Provider Contract Tests
Run provider pact verification tests:
```bash
./gradlew providerContractTests
```

#### Publishing Pact Tests
Publish pact tests to the Pact Broker:
```bash
./gradlew pactPublish
```

**Note**: Ensure `PACT_BROKER_FULL_URL` environment variable is set, or the default `http://localhost:80` will be used.

### Code Coverage
Generate JaCoCo test coverage report:
```bash
./gradlew jacocoTestReport
```

The report will be available at `build/reports/jacoco/test/jacocoTestReport.xml`

### Mutation Testing
Run PITest mutation testing:
```bash
./gradlew pitest
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

    The report will be created in the build/reports subdirectory in your project directory.

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

## Troubleshooting

### Common Issues

**Issue**: `bootRun` fails with Azure authentication error
- **Solution**: Run `az login` to authenticate with Azure CLI

**Issue**: Application fails to start due to missing environment variables
- **Solution**: Ensure VPN is connected and Azure CLI is authenticated. Delete `.aat-env` file and run `./gradlew bootRun` again to reload secrets.

**Issue**: Functional tests fail
- **Solution**: Ensure `em-stitching-api` is running and `.aat-env` file is properly configured

**Issue**: Port 8080 already in use
- **Solution**: Check if another instance is running: `lsof -i :8080` and kill the process if needed

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
