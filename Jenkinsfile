#! groovy
@Library('corda-shared-build-pipeline-steps@5.0.1') _

import com.r3.build.enums.BuildEnvironment
import com.r3.build.utils.PublishingUtils

int cpus = 1
BuildEnvironment buildEnvironment = BuildEnvironment.AMD64_LINUX

PublishingUtils publishingUtils = new PublishingUtils(this)

pipeline {
    agent {
        kubernetes {
            cloud "eks-e2e"
            yaml kubernetesBuildAgentYaml('build', buildEnvironment, cpus)
            idleMinutes 15
            podRetention always()
            nodeSelector ([
                    "kubernetes.io/arch=${buildEnvironment.buildArchitecture.toString().toLowerCase()}",
                    "kubernetes.io/os=${buildEnvironment.buildOperatingSystem.toString().toLowerCase()}",
                    "role=jenkins-agent"
            ].join(','))
            label ([
                    "kubernetes-build-agent",
                    "${cpus}cpus",
                    "${buildEnvironment.buildArchitecture.toString().toLowerCase()}",
                    "${buildEnvironment.buildOperatingSystem.toString().toLowerCase()}"
            ].join('-'))
            showRawYaml false
            defaultContainer 'build'
        }
    }

  parameters {
    booleanParam defaultValue: (isReleaseBranch || isRelease), description: 'Publish artifacts to Artifactory?', name: 'DO_PUBLISH'
  }

    environment {
        GRADLE_PERFORMANCE_TUNING = '--parallel --build-cache'
        GRADLE_USER_HOME = "/host_tmp/gradle"
        ARTIFACTORY_CREDENTIALS = credentials('artifactory-credentials')
        BUILD_CACHE_CREDENTIALS = credentials('gradle-ent-cache-credentials')
        BUILD_CACHE_PASSWORD = "${env.BUILD_CACHE_CREDENTIALS_PSW}"
        BUILD_CACHE_USERNAME = "${env.BUILD_CACHE_CREDENTIALS_USR}"
        CORDA_ARTIFACTORY_PASSWORD = "${env.ARTIFACTORY_CREDENTIALS_PSW}"
        CORDA_ARTIFACTORY_USERNAME = "${env.ARTIFACTORY_CREDENTIALS_USR}"
        CORDA_ARTIFACTORY_REPOKEY = "${isRelease ? 'corda-dependencies' : 'corda-dependencies-dev'}"
        GRADLE_USER_HOME = "/host_tmp/gradle"
        VERSION_SUFFIX = publishingUtils.getVersionSuffix()
        RELEASE_TYPE = publishingUtils.getReleaseType()
        RELEASE_SUFFIX = publishingUtils.getReleaseSuffix()
        RELEASE_VERSION = publishingUtils.getReleaseString()
    }

    stages {
        stage('Clean') {
            steps {
                gradlew('clean')
            }
        }
        stage('Build (assemble)') {
            steps {
                gradlew('assemble')
            }
        }
        stage('Test') {
            steps {
                gradlew('test')
            }
        }
        stage('Publish') {
          when {
            expression { params.DO_PUBLISH }
            beforeAgent true
          }
          steps {
            authenticateGradleWrapper()
            sh "./gradlew --no-daemon -Pcompilation.allWarningsAsErrors=true -Ptests.failFast=false -Ptests.ignoreFailures=true artifactoryPublish"
            }
        }
    }
}

def gradlewMinimumLogging(String... args) {
    def allArgs = args.join(' ')
    sh "${isUnix() ? './gradlew' : './gradlew.bat'} ${allArgs} \${GRADLE_PERFORMANCE_TUNING}"
}
