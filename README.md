# Sentiment Analysis Spring MVC

[![CI](https://github.com/remla25-team6/app/actions/workflows/cicd.yml/badge.svg)](https://github.com/remla25-team6/app/actions/workflows/cicd.yml)

A simple Spring Boot MVC application that analyzes user-submitted reviews for sentiment (positive/negative), persists results, and allows users to provide feedback for retraining.

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.6+
- A running sentiment model REST endpoint at `/predict`

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
