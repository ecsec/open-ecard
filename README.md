About Open eCard and MOBILE-X
=============================

The **Open eCard** project was started in 2012 by industrial and academic experts to provide an open source and cross platform implementation of the eCard-API-Framework ([BSI TR-03112](https://www.bsi.bund.de/DE/Publikationen/TechnischeRichtlinien/tr03112/TR-03112_node.html)) and the related international standard [ISO/IEC 24727](https://www.iso.org/standard/61066.html), through which arbitrary applications can utilize electronic identification (eID), authentication and signatures with suitable smart cards (eCards).
In a high-level perspective the architecture of the eCard-API-Framework consists of the following [layers](https://www.openecard.org/en/ecard-api-framework/overview/):

* Application-Layer
* Identity-Layer
* Service-Access-Layer
* Terminal-Layer

Against the background of the [eIDAS](https://www.eid.as/)-Regulation, the General Data Protection Regulation ([GDPR](https://eur-lex.europa.eu/eli/reg/2016/679/oj)), the [GAIA-X](https://data-infrastructure.eu/) initiative and the ongoing trend towards increased mobility, the Identity-Layer has been subject to an ongoing revision to form **MOBILE-X**, which integrates the [ChipGateway](https://www.oasis-open.org/committees/download.php/60049/ChipGateway-Specification-OASIS.pdf) protocol as well as aspects of Identity Management [ISO/IEC 24760](https://www.iso.org/standard/77582.html) and Privacy Management [ISO/IEC 29101](https://www.iso.org/standard/75293.html) in order to enable electronic signatures and "Self Sovereign Identity".

The artifacts of the project consist of modularized, and to some extent extensible, libraries as well as client implementations such as a Desktop application (richclient) an smartphone apps for Android and iOS.

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

Finally, you can run the Open eCard App from command line:

    $ ./packager/richclient-packager/target/open-ecard/bin/open-ecard

Packaging
-----------

Native packages which are based on a modular runtime image can be built with the new [jpackage](https://openjdk.java.net/jeps/343) tool which is part of [JDK-14](https://jdk.java.net/14/). Native packages for the Open eCard App can be built by downloading JDK-14, referencing it as toolchain and by specifying the following user properties:

    $ mvn clean install -Ddesktop-package -Djlink-jpackager.package-type=<type>

Thereby, the following package types are available:

 - dmg
 - pkg
 - deb
 - rpm
 - exe
 - msi

You have to make sure the required packaging tools are installed. In case of Windows, msi and exe packages are built. For this purpose, two tools are required:

 - [WiX toolset](https://wixtoolset.org/) - to create msi installers
 - [Inno Setup](http://www.jrsoftware.org/isinfo.php) - to create exe installers (Path environment variable must be set)

Native packages for Mac OS can be signed by using the following property (by default, unsigned packages will be created):

    $ mvn clean install -Ddesktop-package -Djlink-jpackager.package-type={dmg, pkg} -Dmac.os.sign.package=true

More information about the required JDK versions and the setup of the toolchain, can be found in the INSTALL.md file.

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
