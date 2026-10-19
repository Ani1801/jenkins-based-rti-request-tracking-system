// =========================================================================
// Declarative Jenkinsfile: Jenkins-Based RTI Request Tracking System (MVP-14)
// Automated CI/CD Lifecycle: Checkout -> Build -> Unit Test -> Selenium E2E
//                            -> Package -> Docker Validation -> Reporting
// =========================================================================

pipeline {
    agent any

    options {
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10', artifactNumToKeepStr: '5'))
        timestamps()
        disableConcurrentBuilds()
    }

    parameters {
        string(name: 'BRANCH_NAME', defaultValue: 'main', description: 'Git branch or tag to build')
        booleanParam(name: 'RUN_SELENIUM_TESTS', defaultValue: true, description: 'Execute headless Chrome Selenium browser tests')
        booleanParam(name: 'RUN_DOCKER_BUILD', defaultValue: true, description: 'Validate and build Docker images')
    }

    environment {
        GIT_REPO_URL = 'https://github.com/Ani1801/jenkins-based-rti-request-tracking-system.git'
    }

    stages {
        // -----------------------------------------------------------------
        // Stage 1: Checkout Source Code
        // -----------------------------------------------------------------
        stage('Checkout') {
            steps {
                echo "Checking out source code from ${env.GIT_REPO_URL}..."
                checkout scm
            }
        }

        // -----------------------------------------------------------------
        // Stage 2: Maven Compilation
        // -----------------------------------------------------------------
        stage('Build') {
            steps {
                echo "Compiling Java 17 application classes using Maven..."
                script {
                    if (isUnix()) {
                        sh 'mvn compile -B'
                    } else {
                        bat 'mvn compile -B'
                    }
                }
            }
        }

        // -----------------------------------------------------------------
        // Stage 3: Unit and Integration Testing
        // -----------------------------------------------------------------
        stage('Unit & Integration Tests') {
            steps {
                echo "Running unit, domain, and service integration tests..."
                script {
                    if (isUnix()) {
                        sh 'mvn test -B'
                    } else {
                        bat 'mvn test -B'
                    }
                }
            }
            post {
                always {
                    junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: false
                }
            }
        }

        // -----------------------------------------------------------------
        // Stage 4: Automated Selenium E2E Browser Testing
        // -----------------------------------------------------------------
        stage('Selenium E2E Tests') {
            when {
                expression { return params.RUN_SELENIUM_TESTS == true }
            }
            steps {
                echo "Evaluating browser prerequisites for Selenium WebDriver..."
                script {
                    def hasChrome = false
                    if (isUnix()) {
                        hasChrome = (sh(script: 'command -v google-chrome || command -v chromium-browser || command -v chromium', returnStatus: true) == 0)
                    } else {
                        hasChrome = (bat(script: 'where chrome.exe >nul 2>&1 || if exist "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe" (exit 0) else (exit 1)', returnStatus: true) == 0)
                    }

                    if (hasChrome) {
                        echo "Compatible Chrome binary located. Launching headless browser test suite..."
                        if (isUnix()) {
                            sh 'mvn test -B -Pselenium -Dtest=RtiWorkflowSeleniumE2ETest'
                        } else {
                            bat 'mvn test -B -Pselenium -Dtest=RtiWorkflowSeleniumE2ETest'
                        }
                    } else {
                        echo "WARNING: Google Chrome is not installed on this Jenkins agent."
                        echo "Skipping browser E2E tests. To enable, provision Google Chrome on the agent host."
                        unstable("Selenium E2E tests skipped: No Chrome browser found on agent")
                    }
                }
            }
            post {
                always {
                    junit testResults: 'target/surefire-reports/*Selenium*.xml', allowEmptyResults: true
                }
            }
        }

        // -----------------------------------------------------------------
        // Stage 5: Artifact Packaging
        // -----------------------------------------------------------------
        stage('Package') {
            steps {
                echo "Packaging Spring Boot executable JAR archive..."
                script {
                    if (isUnix()) {
                        sh 'mvn package -B -DskipTests'
                    } else {
                        bat 'mvn package -B -DskipTests'
                    }
                }
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true, allowEmptyArchive: false
                }
            }
        }

        // -----------------------------------------------------------------
        // Stage 6: Docker Validation & Build
        // -----------------------------------------------------------------
        stage('Docker Validation & Build') {
            when {
                expression { return params.RUN_DOCKER_BUILD == true }
            }
            steps {
                echo "Validating Docker Compose configuration and agent daemon access..."
                script {
                    def hasDockerCli = false
                    if (isUnix()) {
                        hasDockerCli = (sh(script: 'command -v docker', returnStatus: true) == 0)
                    } else {
                        hasDockerCli = (bat(script: 'where docker.exe >nul 2>&1', returnStatus: true) == 0)
                    }

                    if (!hasDockerCli) {
                        error("Docker CLI is not installed or not found on PATH on this Jenkins agent.")
                    }

                    // Prepare local .env from .env.example if .env does not exist
                    if (isUnix()) {
                        sh '''
                            if [ ! -f .env ]; then
                                cp .env.example .env
                            fi
                            docker compose config
                        '''
                    } else {
                        bat '''
                            if not exist .env copy .env.example .env
                            docker compose config
                        '''
                    }

                    // Check if Docker daemon is active and responsive
                    def hasDockerDaemon = false
                    if (isUnix()) {
                        hasDockerDaemon = (sh(script: 'docker info > /dev/null 2>&1', returnStatus: true) == 0)
                    } else {
                        hasDockerDaemon = (bat(script: 'docker info >nul 2>&1', returnStatus: true) == 0)
                    }

                    if (hasDockerDaemon) {
                        echo "Docker daemon active. Building container images..."
                        if (isUnix()) {
                            sh 'docker compose build'
                        } else {
                            bat 'docker compose build'
                        }
                    } else {
                        echo "WARNING: Docker daemon is not active on this agent."
                        echo "Compose configuration syntax validated successfully. Container build requires a running Docker daemon."
                        unstable("Docker daemon unavailable on agent: Container build skipped after syntax validation")
                    }
                }
            }
        }
    }

    // ---------------------------------------------------------------------
    // Post-Build Reporting & Notifications
    // ---------------------------------------------------------------------
    post {
        always {
            echo "Build execution completed for ${currentBuild.fullDisplayName}"
        }
        success {
            echo "==========================================================="
            echo " SUCCESS: RTI Request Tracking System CI Pipeline Passed!  "
            echo " Executable JAR artifact packaged and test reports logged. "
            echo "==========================================================="
        }
        unstable {
            echo "==========================================================="
            echo " UNSTABLE: Pipeline completed with non-fatal warnings.     "
            echo " Check agent prerequisites (Chrome or Docker daemon).      "
            echo "==========================================================="
        }
        failure {
            echo "==========================================================="
            echo " FAILURE: Jenkins Pipeline Encountered an Error.           "
            echo " Review failed test reports and console output above.      "
            echo "==========================================================="
        }
    }
}
