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

    environment {
        GRADLE_PERFORMANCE_TUNING = '--parallel --build-cache'
        GRADLE_USER_HOME = "/host_tmp/gradle"
        ARTIFACTORY_CREDENTIALS = credentials('artifactory-credentials')
        BUILD_CACHE_CREDENTIALS = credentials('gradle-ent-cache-credentials')
        BUILD_CACHE_PASSWORD = "${env.BUILD_CACHE_CREDENTIALS_PSW}"
        BUILD_CACHE_USERNAME = "${env.BUILD_CACHE_CREDENTIALS_USR}"
        CORDA_ARTIFACTORY_PASSWORD = "${env.ARTIFACTORY_CREDENTIALS_PSW}"
        CORDA_ARTIFACTORY_USERNAME = "${env.ARTIFACTORY_CREDENTIALS_USR}"
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
        stage('Prepare Helm charts') {
            environment {
                HELM_CHART_VERSION = publishingUtils.getHelmChartVersion()
                HELM_CHART_APP_VERSION = getGradleProperty("version")
            }
            steps {
                script {
                    publishingUtils.prepareHelmChart('artifactory-credentials')
                }
            }
        }
        stage('Publish / Publish Images') {
            environment {
                BASE_IMAGE = 'docker-remotes.software.r3.com/azul/zulu-openjdk'
                BASE_IMAGE_TAG = '11.0.15-11.56.19'
            }
            steps {
                script {
                    gradlewMinimumLogging(
                        'publishOSGiImage',
                        '-PjibRemotePublish=true',
                        "-PworkerBaseImageTag=${env.BASE_IMAGE_TAG}",
                        "-PbaseImage=${env.BASE_IMAGE}",
                        "-PuseDockerDaemon=false"
                    )
                }
            }
            post {
                success {
                    script{
                        renderWidget("Release artifacts version: ${getGradleProperty("version")}")
                    }
                }
            }
        }
        stage('Publish Helm Chart to Artifactory') {
            /*
            * Depending on the situation, Helm charts are published to the following repositories with the given chart version:
            * See {@link PublishingUtils#getHelmRepo()} for details on how helm chart repo name is generated
            *
            * Release tag          corda-os-docker-stable.software.r3.com/helm-charts/<branch-name>      <CHART_VERSION>
            * e.g.  corda-os-docker-stable.software.r3.com/helm-charts/5.0.0.0-Beta3/corda
            *
            * Release/main branch  corda-os-docker-unstable.software.r3.com/helm-charts/<branch-name>    <CHART_VERSION>-beta.<TIMESTAMP>
            * e.g. corda-os-docker-unstable.software.r3.com/helm-charts/release/os/5.0/corda
            *
            * PR / feature branch  corda-os-docker-dev.software.r3.com/helm-charts/<branch-name>         <CHART_VERSION>-alpha.<TIMESTAMP>
            * e.g. corda-os-docker-unstable.software.r3.com/helm-charts/PR-223/corda
            */
            environment {
                HELM_REGISTRY = publishingUtils.getInternalRegistry(false)
                HELM_CHART_VERSION = publishingUtils.getHelmVersionFromYaml()
                HELM_CHART_REPO_NAME = publishingUtils.getHelmRepo()
            }
            steps {
                script {
                    publishingUtils.publishHelmCharts('artifactory-credentials')
                }
            }
            post {
                success {
                    script{
                        renderWidget("Published Corda helm chart version to Artifactory: ${env.HELM_CHART_VERSION}")
                        renderWidget("Published Corda helm chart repo name: ${env.HELM_CHART_REPO_NAME}")
                        renderWidget("Published Corda helm chart OCI path: oci://${env.HELM_REGISTRY}/${env.HELM_CHART_REPO_NAME}/corda")
                    }
                }
            }
        }
    }
}

def gradlewMinimumLogging(String... args) {
    def allArgs = args.join(' ')
    sh "${isUnix() ? './gradlew' : './gradlew.bat'} ${allArgs} \${GRADLE_PERFORMANCE_TUNING}"
}
