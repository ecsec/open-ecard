About Open eCard
================

In the context of the Open eCard Project, industrial as well as academic
experts have decided to work together on providing an open source and cross
platform implementation of the eCard-API-Framework (BSI-TR-03112), through
which arbitrary applications can utilize authentication and signatures with
arbitrary chip cards.

The artifacts of the project consist of modularized, and to some extent
extensible, libraries as well as client implementations such as a Desktop
application (richclient), an Android app and a Java Applet.


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

    $ ./clients/richclient-jlink/target/open-ecard/bin/openecard 


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
