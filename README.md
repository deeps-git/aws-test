# IpGatewayProvisioning

## Build 

<p>

###### Build all projects as defined in settings.gradle
cd $projectDir
<p>
./gradlew -C rebuild clean build -i

<p>


###### Build single project
cd $projectDir
<p>
./gradlew :\<subproject\>:build

OR

cd $projectDir/\<subproject\>
<p>
../gradlew -C rebuild clean build -i
<p>


## Create new project shell
cd $projectDir/scripts
<p>
../gradlew build

cd $projectDir/scripts
<p>
../gradlew usage
<p>


* Windows shell users may need to replace slash '/' with the backslash '\\'
