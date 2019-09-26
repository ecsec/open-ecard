Prerequisites
=============

In order to build the Open eCard project, some additional tools are needed.

Required dependencies are:
* Java JDK 11 or higher - Oracle JDK and OpenJDK are working correctly (jlink is required for building modular runtime images)

* Maven in at least version 3.6.1

  https://maven.apache.org/download.html

* Git 1.7.11 or higher (older versions are probably also ok)

  http://git-scm.com/downloads

Optional dependencies are:
* Java JDK 14 with the new jpackage tool for creating native packages (early-access builds are available)

  https://jdk.java.net/jpackage/)

* Android SDK
  The Android SDK dependent modules are built when the environment variable
  ANDROID_HOME is set and points to the installation directory of the Android
  SDK.

  https://developer.android.com/sdk/index.html

* Android NDK
  The Android NDK dependent modules are built when the environment variable
  ANDROID_NDK_HOME is set and points to the installation directory of the
  Android NDK. The Android NDK has a direct dependency for the Android
  SDK. However due to restrictions in maven, no actual check is performed
  enforcing that the Android SDK must also be configured.

  https://developer.android.com/tools/sdk/ndk/index.html

Native packages can be created with an early-access build of JDK-14 which contains the new jpackage tool. For this purpose, JDK-14 must be managed independently of other JDK versions. This can be done with Maven toolchains. For this purpose, a `toolchains.xml` file is required on the building machine. The default location of this file is `~/.m2/toolchains.xml`. It should look similar to this one (JDK paths must be 	adjusted):

```xml
<?xml version="1.0" encoding="UTF8"?>
<toolchains>
	<!-- JDK toolchains -->
        <toolchain>
                <type>jdk</type>
                <provides>
                        <version>14</version>
                        <vendor>OpenJDK</vendor>
                        <id>JavaSE-14</id>
                </provides>
                <configuration>
			<jdkHome>/usr/lib/jvm/jdk-14/</jdkHome>
                </configuration>
        </toolchain>
        <toolchain>
                <type>jdk</type>
                <provides>
                        <version>11</version>
                        <vendor>OpenJDK</vendor>
                        <id>JavaSE-11</id>
                </provides>
                <configuration>
			<jdkHome>/usr/lib/jvm/java-11-openjdk-amd64/</jdkHome>
                </configuration>
        </toolchain>
</toolchains>
```

If native packages are not created on the building machine, the first toolchain is not required.

Build Sources
=============

A standard build is performed by the command:

    $ mvn clean install

In order to create Javadoc and source artifacts, perform the following command:

    $ mvn clean javadoc:javadoc javadoc:jar source:jar install

By default, only a modular runtime image is created. However, if the early-access build of JDK-14 with the new jpackage tool is added to the toolchains, a native application package can be created by using the property `desktop-package`:

    $ mvn clean install -Ddesktop-package

Usually, the predefined package formats are used: dmg for Mac OS, deb for Linux and msi and exe for Windows. An additional property `jlink-jpackager.package-type` can replace the predefined format of the native application package (only for Mac and Linux). The possible formats are:

 - dmg
 - pkg
 - deb
 - rpm

A native package with the `pkg` format can be created by using the following command:

    $ mvn clean install -Ddesktop-package -Djlink-jpackager.package-type=pkg

The developer has to make sure that all necessary packaging tools are installed. In case of Windows, msi and exe packages are built. For this purpose, two additional tools are required:

 - [WiX toolset](https://wixtoolset.org/) - to create msi installers
 - [Inno Setup](http://www.jrsoftware.org/isinfo.php) - to create exe installers (Path environment variable must be set)

Build Profiles
--------------

The Open eCard project uses Maven profiles to modify how the build is
performed and which artifacts are created.

Maven profiles are selected on the commandline by adding the -P option as
follows:

    $ mvn -Pprofile1,profile2 <Maven goals>


The following global profiles are defined:
* `release`
  Remove debugging symbols from Java bytecode.

The following profiles are module specific:

Module `clients/applet`
* `trace-applet`
  Bundle SLF4J extension artifact, so that the applet can emit trace logs.

Module `clients/richclient`
* `bundles-jar-cifs`
  Usually the cifs are provided by a seperate artifact. This profile creates
  a 'contains everything' jar file for direct execution.


Code Signing
------------

The `applet` and `richclient` modules produce signed artifacts. A dummy
certificate is included in the source distribution, so the build runs in any
case. If another certificate from a trusted CA should be used, then the
following fragment must be inserted into `$HOME/.m2/settings.xml`:

  <profiles>
    <profile>
      <id>override-sign</id>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <properties>
        <sign.keystore>PATH_TO_JAVA_KEYSTORE</sign.keystore>
        <sign.storepass>KEYSTORE_PASSWORD</sign.storepass>
        <sign.keypass>CERTIFICATE_KEY_PASSWORD</sign.keypass>
        <sign.alias>CERTIFICATE_ALIAS</sign.alias>
      </properties>
    </profile>
  </profiles>
