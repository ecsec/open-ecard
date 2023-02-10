#!/usr/bin/env bash

# set variable SIMULATE to only generate jars without uploading

if [ -z "$API_VERSION" ]; then
    echo "No API_VERSION defined, quitting ..."
    exit 1
fi
API_VERSION_NAME=${API_VERSION_NAME:-$API_VERSION}

if [ -z "$ANDROID_HOME" ]; then
    echo "No ANDROID_HOME defined, quitting ..."
    exit 1
fi

content="content"
function strip_unwanted() {
	work_dir=$(mktemp -d)

	cd $work_dir
	mkdir "$content"

	cd "$content"
	jar -x -f "$1"
	cd ..
}

strip_unwanted "$ANDROID_HOME/platforms/android-$API_VERSION_NAME/android.jar"
echo "Stub work dir: $work_dir"
jar_archive_stub="$work_dir/stripped.jar"
jar -c -f "$jar_archive_stub" \
	-C "$content" android \
	-C "$content" androidx \
	-C "$content" assets \
	-C "$content" dalvik \
	-C "$content" NOTICES \
	-C "$content" res
pom_file="$work_dir/pom.xml"
cat << __end_of_pom__ > "$pom_file"
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
		 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		 xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>

	<groupId>com.google.android</groupId>
	<artifactId>android</artifactId>
	<version>api-$API_VERSION</version>
	<packaging>jar</packaging>

	<name>Google Android Library</name>
	<description>A library jar that provides APIs for Applications written for the Google Android Platform.</description>
	<url>http://source.android.com/</url>

	<dependencies>
		<!-- org.apache.commons.logging -->
		<dependency>
			<groupId>commons-logging</groupId>
			<artifactId>commons-logging</artifactId>
			<version>1.1.1</version>
		</dependency>
		<!-- org.apache.http.* -->
		<dependency>
			<groupId>org.apache.httpcomponents</groupId>
			<artifactId>httpclient</artifactId>
			<version>4.0.1</version>
		</dependency>
		<!-- javax.microedition.khronos.* -->
		<dependency>
			<groupId>org.khronos</groupId>
			<artifactId>opengl-api</artifactId>
			<version>gl1.1-android-2.1_r1</version>
		</dependency>
		<!-- org.xml.sax.*, org.w3c.dom.* -->
		<dependency>
			<groupId>xerces</groupId>
			<artifactId>xmlParserAPIs</artifactId>
			<version>2.6.2</version>
		</dependency>
		<!-- org.xmlpull.v1.* -->
		<dependency>
			<groupId>xpp3</groupId>
			<artifactId>xpp3</artifactId>
			<version>1.1.4c</version>
		</dependency>
		<!-- org.json.* -->
		<dependency>
			<groupId>org.json</groupId>
			<artifactId>json</artifactId>
			<version>20080701</version>
		</dependency>
	</dependencies>
</project>
__end_of_pom__

strip_unwanted "$ANDROID_HOME/platforms/android-$API_VERSION_NAME/android-stubs-src.jar"
echo "Source work dir: $work_dir"
jar_archive_source="$work_dir/stripped.jar"
jar -c -f "$jar_archive_source" \
	-C "$content" android \
	-C "$content" dalvik

if [ -z "$SIMULATE" ]; then
    echo "Uploading artifatcs ..."

    mvn deploy:deploy-file -DrepositoryId=ecsec-thirdparty -Durl=https://mvn.ecsec.de/repository/ecard_thirdparty/ \
    	-DgroupId=com.google.android -DartifactId=android -Dversion="api-$API_VERSION" \
    	-Dfile="$jar_archive_stub"

    mvn deploy:deploy-file -DrepositoryId=ecsec-thirdparty -Durl=https://mvn.ecsec.de/repository/ecard_thirdparty/ \
    	-DgroupId=com.google.android -DartifactId=android -Dversion="api-$API_VERSION" -Dclassifier=sources \
    	-Dfile="$jar_archive_source"

    mvn deploy:deploy-file -DrepositoryId=ecsec-thirdparty -Durl=https://mvn.ecsec.de/repository/ecard_thirdparty/ \
    	-DgroupId=com.google.android -DartifactId=android -Dversion="api-$API_VERSION" -Dpackaging=pom \
    	-Dfile="$pom_file"

else
	echo "Deployment simulated, not uploading artifacts ..."
fi
