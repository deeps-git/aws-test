//Jenkinsfile

env.BUILD_BASE_PATH="/opt/"
env.BUILD_DIR="temp"
env.REPO_TO_BUILD="IpGatewayProvisioning"
env.BUILD_SCRIPTS_REPO="bamboo_deployment_projects"
env.BUILD_MAVEN_PATH="com/comcast/xcal/crpl/awslambda/support"
env.REPO_URL="https://github.com/deeps-git/bamboo_deployment_projects.git"

pipeline {
  agent any
    stages {
      stage('Exporting env variables and checkout') {
           steps {
             /*
               sh 'export BUILD_BASE_PATH=/opt'
               sh 'export BUILD_DIR=temp'
               sh 'export REPO_TO_BUILD=IpGatewayProvisioning'
               sh 'export BUILD_SCRIPTS_REPO=bamboo_deployment_projects'
               sh 'export BUILD_MAVEN_PATH=com/comcast/xcal/crpl/awslambda/support'
               sh 'export REPO_URL=git@github.comcast.com:CRPL/bamboo_deployment_projects.git'
               */
               echo 'Checking out bamboo_deployment_projects.git'
               git branch: 'master', credentialsId: 'testing', url: 'https://github.com/deeps-git/bamboo_deployment_projects.git'
               echo "BUILD_BASE_PATH is ${BUILD_BASE_PATH}"
               echo "BUILD_DIR is ${BUILD_DIR}"
               echo "REPO_TO_BUILD is ${REPO_TO_BUILD}"
               echo "BUILD_SCRIPTS_REPO is ${BUILD_SCRIPTS_REPO}"
               echo "BUILD_MAVEN_PATH is ${BUILD_MAVEN_PATH}"
               echo "REPO_URL is ${REPO_URL}"
                 sh 'whoami'
                 sh 'cd /Users/Shared/Jenkins/Home/workspace/aws-terraform/CRPLSupportFunctions_scripts/workingSetup/ && ./gitpull.sh'
           }
           }
      }

}
