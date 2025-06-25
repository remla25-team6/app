# Sentiment Analysis Spring MVC

[![CI](https://github.com/remla25-team6/app/actions/workflows/cicd.yml/badge.svg)](https://github.com/remla25-team6/app/actions/workflows/cicd.yml)

A simple Spring Boot MVC application that analyzes user-submitted reviews for sentiment (positive/negative), persists results, and allows users to provide feedback for retraining.

## API & Frontend Overview

| **Path**               | **Method** | **Description**                                                                   |
| ---------------------- | ---------- | --------------------------------------------------------------------------------- |
| `/`                    | `GET`      | Renders the homepage with a review form, version info, and past inferences.       |
| `/`                    | `POST`     | Submits a review for sentiment inference. Result is stored and shown on page.     |
| `/train`               | `POST`     | Submits corrections (disagreed predictions). Flips sentiment and stores feedback. |
| `/train`               | `GET`      | Returns all user feedback entries as JSON for retraining or inspection.           |
| `/actuator/prometheus` | `GET`      | Exposes Prometheus-formatted metrics via Micrometer for observability.            |

### Feature Summary

| **Feature**         | **Details**                                                                                               |
| ------------------- | --------------------------------------------------------------------------------------------------------- |
| Review Submission   | Users enter a review via a form. It is sent to the model API (`/predict` via `MODEL_URL`) and classified. |
| Result Display      | All prior predictions are shown on the homepage, along with version info and a feedback checkbox.         |
| Feedback Submission | Users can select incorrect predictions and submit feedback. The system stores flipped sentiments.         |
| Feedback API        | Accessible at `/train` (`GET`) as JSON. Useful for training data collection.                              |
| Metrics             | Inference time, failure counts, and stored review counts are tracked and exposed via Prometheus.          |

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.6+
- A running sentiment model REST endpoint at `/predict`
- GitHub-hosted dependency: `lib-version`s (via GitHub Packages)

### Installation
#### With Docker
1. Download app package from the repository or from ghcr.io/remla25-team6/app
2. Run it with docker
   ```bash
   docker run -p 8080:8080 -e MODEL_URL='<MODEL_URL>' app
   ```
#### Without Docker
1. Clone the repository:
   ```bash
   git clone https://github.com/remla25-team6/app.git
   cd app
   ```
2. Configure the following environment variable:
    ```bash
    MODEL_URL=<YOUR MODEL URL>
    ```
3. Authenticate Maven to GitHub repository via PAT:
    ```
    // in /.m2/settings.xml
    <settings>
      <servers>
        <server>
          <id>github</id>
          <username>YOUR USERNAME</username>        <!-- your GitHub username -->
          <password>YOUR PAT</password>    <!-- your PAT  -->
        </server>
      </servers>
    </settings>
    ```
4. Build the project with Maven:
    ```bash
    mvn clean package
    ```

## Monitoring
Micrometer metrics are available at `/actuator/prometheus` and can be scraped by Prometheus. Custom metrics are configured via `WebMetrics.java` and `MetricsConfig.java`.

##  CI/CD
This project uses GitHub Actions to automate testing and versioned releases.

### CI
Every commit and pull request triggers the `cicd.yml` workflow, which:
- Builds the application using Maven
- Runs unit tests
- Verifies build and integration success

### Release
To publish an official release:
1. Ensure all changes are committed and pushed to a release-ready branch.
2. Tag the commit with a semantic version like `v0.1.0` and push:
  ```bash
  git tag v0.1.0
  git push origin v0.1.0
  ```
3. This triggers the release.yml workflow, which:
  - Builds the application
  - Publishes a GitHub Release with the JAR file attached

### Pre-release
To publish a pre-release:
1. Push a commit to the `main` branch (e.g. through a pull request to `main`).
2. The `prerelease.yml` workflow runs automatically on each commit to main:
  - Builds and packages the app
  - Publishes a pre-release on GitHub with a timestamped version (e.g., `v0.1.0-pre.20250625.185600`)