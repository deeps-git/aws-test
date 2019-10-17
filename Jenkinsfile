//Jenkinsfile

env.BUILD_BASE_PATH="/test"
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
               dir('bamboo') {
               git branch: 'master', credentialsId: 'testing', url: 'https://github.com/deeps-git/bamboo_deployment_projects.git'
               }
               dir('a-child-repo') {
               git branch: 'master', credentialsId: 'testing', url: 'https://github.com/deeps-git/IpGatewayProvisioning.git'
                 }
               echo "BUILD_BASE_PATH is ${BUILD_BASE_PATH}"
               echo "BUILD_DIR is ${BUILD_DIR}"
               echo "REPO_TO_BUILD is ${REPO_TO_BUILD}"
               echo "BUILD_SCRIPTS_REPO is ${BUILD_SCRIPTS_REPO}"
               echo "BUILD_MAVEN_PATH is ${BUILD_MAVEN_PATH}"
               echo "REPO_URL is ${REPO_URL}"
               sh 'whoami'
               sh 'cd /Users/Shared/Jenkins/Home/workspace/aws-terraform/CRPLSupportFunctions_scripts/workingSetup/ && ./gitpull.sh'
               echo "Checking build type"
               //sh 'pushd ${BUILD_BASE_PATH}/${BUILD_DIR}/${REPO_TO_BUILD}'
           }
      }
         stage('Checking release version') {
               steps {
                    echo 'Checking out REPO_TO_BUILD'
                    git branch: 'master', credentialsId: 'testing', url: 'https://github.com/deeps-git/IpGatewayProvisioning.git'
                script{
                  //echo 'Checking build type'
                  //sh 'pushd ${BUILD_BASE_PATH}/${BUILD_DIR}/${REPO_TO_BUILD}'
                  sh 'releaseVersion= $(./gradlew properties | grep releaseVersion | cut -d" " -f2);'
     sh '''
             if [[ -z "${releaseVersion}" ]]; then
                 releaseVersion="0-SNAPSHOT"
                 ENVIRONMENT="Dev"
                 echo "Looks like a Dev build, releaseVersion=0-SNAPSHOT"
             else
                 echo "Looks like a Staging build, releaseVersion="${releaseVersion}
                 ENVIRONMENT="Stage"
                 echo "Creating tag"
                 git tag -a ${releaseVersion} -m"${releaseVersion}"
                 git push --follow-tags
                 echo "Running Gradle wrapper to -::::rebuild clean install uploadArchives::::-"
             fi
               git fetch --tags ${REPO_URL}
               export RELEASE_VERSION=${releaseVersion}
               export ENVIRONMENT="-"${ENVIRONMENT};
              ./gradlew -C rebuild clean build uploadArchives -i
               popd

               echo "Following are the projects:"
               echo ""
               ./pnameprocess.sh
         ''' 
           }
         }
      }
    }
}
