//Jenkinsfile

def BUILD_BASE_PATH="/opt/"
def BUILD_DIR="temp"
def REPO_TO_BUILD="IpGatewayProvisioning"
def BUILD_SCRIPTS_REPO="bamboo_deployment_projects"
def BUILD_MAVEN_PATH="com/comcast/xcal/crpl/awslambda/support"
def REPO_URL="git@github.comcast.com:CRPL/bamboo_deployment_projects.git"

/*
pipeline {
  agent any
    stages {
      stage('Exporting env variables and checkout') {
           steps {
               sh 'export BUILD_BASE_PATH=/opt'
               sh 'export BUILD_DIR=temp'
               sh 'export REPO_TO_BUILD=IpGatewayProvisioning'
               sh 'export BUILD_SCRIPTS_REPO=bamboo_deployment_projects'
               sh 'export BUILD_MAVEN_PATH=com/comcast/xcal/crpl/awslambda/support'
               sh 'export REPO_URL=git@github.comcast.com:CRPL/bamboo_deployment_projects.git'
               echo 'Checking out bamboo_deployment_projects.git'
               git branch: 'dev', credentialsId: '5ba3567c-bfb0-451f-bdf0-6431c3e309b5', url: 'git@github.comcast.com:CRPL/bamboo_deployment_projects.git'
               sh 'cd /var/lib/jenkins/workspace/IpGatewayProvisioning-DevStage/CRPLSupportFunctions_scripts/workingSetup/ && ./gitpull.sh'
           }
           }
      }

}
