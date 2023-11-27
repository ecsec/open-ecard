About Open eCard and MOBILE-X
=============================

The **Open eCard** project was started in 2012 by industrial and academic experts to provide an open source and cross platform implementation of the eCard-API-Framework ([BSI TR-03112](https://www.bsi.bund.de/DE/Publikationen/TechnischeRichtlinien/tr03112/TR-03112_node.html)) and the related international standard [ISO/IEC 24727](https://www.iso.org/standard/61066.html), through which arbitrary applications can utilize electronic identification (eID), authentication and signatures with suitable smart cards (eCards).
In a high-level perspective the architecture of the eCard-API-Framework consists of the following [layers](https://www.openecard.org/en/ecard-api-framework/overview/):

* Application-Layer
* Identity-Layer
* Service-Access-Layer
* Terminal-Layer

Against the background of the [eIDAS](https://www.eid.as/)-Regulation, the General Data Protection Regulation ([GDPR](https://eur-lex.europa.eu/eli/reg/2016/679/oj)), the [GAIA-X](https://data-infrastructure.eu/) initiative and the ongoing trend towards increased mobility, the Identity-Layer has been subject to an ongoing revision to form **MOBILE-X**, which integrates the [ChipGateway](https://www.oasis-open.org/committees/download.php/60049/ChipGateway-Specification-OASIS.pdf) protocol as well as aspects of Identity Management [ISO/IEC 24760](https://www.iso.org/standard/77582.html) and Privacy Management [ISO/IEC 29101](https://www.iso.org/standard/75293.html) in order to enable electronic signatures and "Self Sovereign Identity".

The artifacts of the project consist of modularized, and to some extent extensible, libraries for Desktop applications and smartphone apps for Android and iOS as well as client implementations like a Desktop application (richclient) for various operating systems.


Build Instructions
==================

Detailed build instructions can be found in the INSTALL.md file bundled with
this source package.

Quick Start
-----------

The simplified build instructions are as follows:

    $ git clone git://github.com/ecsec/open-ecard.git
    $ cd open-ecard
    $ mvn clean install


In case you received a preassembled source bundle, the build instructions are
as follows:

    $ tar xaf open-ecard-${version}.tar.xz
    $ cd open-ecard-$version
    $ mvn clean install

Finally, you can run the Open eCard App richclient from command line:

    $ ./mvnw exec:java -pl clients/richclient -Dexec.mainClass=org.openecard.richclient.RichClient

Packaging
-----------

Native packages which are based on a modular runtime image can be built with the new [jpackage](https://openjdk.java.net/jeps/343) tool which is part in newer JDK versions (14+). Native packages for the Open eCard can be built by downloading such a JDK version, referencing it as toolchain and by specifying the following property:

    $ mvn clean install -Ddesktop-package

By default, the packager will take the predefined package types, such as dmg for Mac OS and deb for Linux-based systems. The package type can be overridden for Mac and Linux packages by using the following user property:

    $ mvn clean install -Ddesktop-package -Dpackage.type=<type>

Thereby, the following types are available:

 - DMG
 - PKG
 - DEB
 - RPM

You have to make sure the required packaging tools are installed. In case of Windows, msi and exe packages are built. For this purpose, two additional tools are required:

 - [WiX toolset](https://wixtoolset.org/) - to create msi installers
 - [Inno Setup](http://www.jrsoftware.org/isinfo.php) - to create exe installers (Path environment variable must be set)

More information about the required JDK versions and the setup of the toolchain, can be found in the INSTALL.md file.

Mobile libs
-----------

Open eCard supports building of libraries for Android and iOS for usage in arbitrary mobile apps.
For this purpose, Java JDK 17 is required.
Building the Open eCard with those mobile libraries can look like the following:

    $ ./mvnw clean install -pl packager/ios-framework -am -P build-mobile-libs

### Android
After a successfull build the library for android can be found in `android-lib` sub project.
It also can be used as prebuild dependency via gradle dependency management.
See [open-ecard-android](https://github.com/ecsec/open-ecard-android) for further information.

The android API artifacts are taken from the Android SDK.
They are uploaded into the maven repository with the following commands.
Remember to set the variables `API_VERSION` and `ANDROID_HOME` accordingly.

```
mvn deploy:deploy-file -DrepositoryId=ecsec-thirdparty  -Durl=https://mvn.ecsec.de/repository/ecard_thirdparty/ -DgroupId=com.google.android -DartifactId=android -Dversion=api-$API_VERSION -Dclassifier=sources -Dfile=$ANDROID_HOME/platforms/android-$API_VERSION/android-stubs-src.jar
mvn deploy:deploy-file -DrepositoryId=ecsec-thirdparty  -Durl=https://mvn.ecsec.de/repository/ecard_thirdparty/ -DgroupId=com.google.android -DartifactId=android -Dversion=api-$API_VERSION -Dfile=$ANDROID_HOME/platforms/android-$API_VERSION/android.jar
```

### iOS (>2.x)
If building on MacOS a ready to use framework gets generated and can be found in
`./packager/ios-framework/target/robovm`. 
The framework can also be found as asset of the release.

It also can be installed as a cocoapod dependency.
```
pod 'open-ecard'
```
See [open-ecard-ios](https://github.com/ecsec/open-ecard-ios) for further information.


Changelog
=========

A release changelog can be created by listing all commits since the last release:

```
git log --oneline --no-merges ${LAST_RELEASE_TAG}..${NEW_RELEASE_TAG}
```

Usually, this list will be manually adjusted by removing unimportant commits.
For example, commits that are related to the CI process.


License
=======

The Open eCard App uses a Dual Licensing model. The software is always
distributed under the GNU General Public License v3 (GPLv3). Additionally the
software can be licensed in an individual agreement between the licenser and
the licensee.


Contributing
============

New developers can find information on how to participate under
https://dev.openecard.org/projects/open-ecard/wiki/Developer_Guide.

Contributions can only be accepted when the contributor has signed the
contribution agreement (https://dev.openecard.org/documents/35). The agreement
basically states, that the contributed work can, additionally to the GPLv3, be
made available to others in an individual agreement as defined in the previous
section. For further details refer to the agreement.

Release (CD)
============
The creation of precompiled artifacts is done via a CD pipeline on servers of
ecsec GmbH.
To perform a new release, the version information in the pom files has to be
updated and an appropriate tag in the form: vX.YY.ZZZ has to be pushed to the
repository clone on ecsecs gitlab instance.
To update the version one can use:

```
mvn versions:set -DnewVersion=X.YY.ZZZ
```

After the release, consider updating the version again to a new SNAPSHOT version.
