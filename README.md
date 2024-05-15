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

Note that command line examples are shown in unix style.
On windows all examples should work analogously with the appropriate style.
Instead of `./gradlew` use `.\gradlew.bat` on windows machines.

Quick Start
-----------

For a general overview of available gradle tasks use: 

	$ ./gradlew tasks 

The simplified build instructions are as follows:

    $ git clone git://github.com/ecsec/open-ecard.git
    $ cd open-ecard
    $ ./gradlew build


In case you received a pre-assembled source bundle, the build instructions are
as follows:

    $ tar xaf open-ecard-${version}.tar.xz
    $ cd open-ecard-$version
    $ ./gradlew build

Finally, you can run the Open eCard App richclient from command line:

    $ ./gradlew :clients:richclient:run

Packaging
-----------

Native packages which are based on a modular runtime image can be built with [jpackage](https://docs.oracle.com/en/java/javase/21/jpackage/).
The following types are available:

- DMG
- PKG
- DEB
- RPM
- MSI
- EXE

Native packages for the Open eCard can be built by the following commands, given the required build tools are available.

    $ ./gradlew :clients:richclient:pacakgeDmg
    $ ./gradlew :clients:richclient:pacakgePkg
    $ ./gradlew :clients:richclient:pacakgeDeb
    $ ./gradlew :clients:richclient:pacakgeRpm
    $ ./gradlew :clients:richclient:pacakgeMsi
    $ ./gradlew :clients:richclient:pacakgeExe

Or to build all types for a platform:

    $ ./gradlew :clients:richclient:packageWin
    $ ./gradlew :clients:richclient:packageLinux
    $ ./gradlew :clients:richclient:packageMac


You have to make sure the required packaging tools are installed.
In case of Windows, msi and exe packages are built. For this purpose, two additional tools are required:

 - [WiX toolset](https://wixtoolset.org/) - to create msi installers
 - [Inno Setup](http://www.jrsoftware.org/isinfo.php) - to create exe installers (Path environment variable must be set)


Mobile libs
-----------

Open eCard supports building of libraries for Android and iOS for usage in arbitrary mobile apps.

### Android
The android artifacts are created during a normal build (e.i. gradle `build` task).
However, the android SDK must be present and configured correctly (e.i. `ANDROID_HOME` must be set).

See [open-ecard-android](https://github.com/ecsec/open-ecard-android) for further information on using the artifact.

### iOS (>2.x)
Build the ios artifacts by running the following command (Apple developer machine is required):

	`./gradlew :clients:ios-framework:build`

If building on MacOS a ready to use framework gets generated and can be found in the ios-framework build folder.

	`./clients/ios-framework/build/robovm` 

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

The Open eCard App uses a Dual Licensing model.
The software is always distributed under the GNU General Public License v3 (GPLv3).
Additionally, the software can be licensed in an individual agreement between the licenser and the licensee.


Contributing
============

New developers can find information on how to participate under https://dev.openecard.org/projects/open-ecard/wiki/Developer_Guide.

Contributions can only be accepted when the contributor has signed the contribution agreement (https://dev.openecard.org/documents/35).
The agreement basically states, that the contributed work can, additionally to the GPLv3, be made available to others in an individual agreement as defined in the previous section.
For further details refer to the agreement.


Release (CD)
============
A release is created by publishing a new version tag.
The creation of precompiled artifacts is done via a CD pipeline on servers of ecsec GmbH.
After a successful build, the artifacts are uploaded to form a github release.
