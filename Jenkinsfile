#!groovy
// https://git.uptake.com/projects/BLT/repos/jenkins-pipeline-utilities/browse
@Library('pipelineUtilities')
import com.uptake.jenkins.pipelines.docker.DockerSupport

def dockerSupport = DockerSupport.create(this)

properties([
        buildDiscarder(
                logRotator(numToKeepStr: '5')
        ),
        parameters([
                booleanParam(
                        name: 'release',
                        description: 'Tag and publish this as a release?',
                        defaultValue: false
                )
        ])
])

def isRelease = false
if (params.release) {
    if (env.BRANCH_NAME == 'master') {
        isRelease = true
    } else {
        error 'Can not run a release on this branch'
    }
}
echo "Is this build a release? ${isRelease}"

node {
    checkout scm
    withCredentials([
            usernamePassword(
                    credentialsId: 'Nexus-UsernamePassword-ReleasesRepository',
                    passwordVariable: 'NEXUS_PASSWORD',
                    usernameVariable: 'NEXUS_USERNAME'
            )
    ]) {
        dockerSupport.insideContainer {
            stage('Build and Test') {
                sh './gradlew build'
            }

            if (env.BRANCH_NAME == 'master') {
                stage('Publish') {
                    if (isRelease) {
                        echo 'Publishing release artifacts'
                        sh './gradlew publish -PisRelease -Prelease.useAutomaticVersion=true'
                    } else {
                        echo 'Publishing snapshot artifacts'
                        sh './gradlew publish'
                    }
                }
                if (isRelease) {
                    stage('Increment') {
                        sshagent(['Bitbucket-SshPrivateKey-CI']) {
                            sh """
                                mkdir -p ~/.ssh
                                echo "Host *" > ~/.ssh/config
                                echo "StrictHostKeyChecking no" >> ~/.ssh/config
                                git config --global user.email "jenkins@uptake.com"
                                git config --global user.name "Jenkins CI Release"
                                git fetch
                                git checkout $BRANCH_NAME
                                git reset --hard origin/$BRANCH_NAME
            
                                ./gradlew release -PisRelease -Prelease.useAutomaticVersion=true
                                """
                        }
                    }
                }
            }
        }
    }
}
