Project Setup
=============

iOS
---

The Open eCard Framework for iOS is delivered as a Swift and ObjC compatible framework which can be added to a project in XCode.
The framework must then be "embedded and signed" during the build phase of XCode.


As an alternative the framework is also available via `CocoaPods <https://cocoapods.org/>`_.
Within the Podfile the following has to be specified: 

.. code-block::
   :substitutions:

   use_frameworks!
   pod 'open-ecard', '~> |releaseMinor|'


Since the framework uses NFC technology, within "Capabilities and Signing" the "Near field communication" capability has to be activated.

As described in the `Apple Developer documentation <https://developer.apple.com/documentation/corenfc/nfciso7816tag>`_
the app must include the following to be able to detect and communicate with ISO7816 tags:

The Near Field Communication Tag Reader Session Formats Entitlement:

.. code-block:: xml
   
   <key>com.apple.developer.nfc.readersession.formats</key>
   <array>
     <string>TAG</string>
   </array>


A list of supported application identifiers of ISO7816 tags within the Info.plist file:

.. code-block:: xml

    <key>com.apple.developer.nfc.readersession.iso7816.select-identifiers</key>
    <array>
      <!-- ICAO ePassport compatible token such as the German eID -->
      <string>A0000002471001</string>
    </array>


The Info.plist must also contain the ``<NFCReaderUsageDescription>`` key with a value properly explaining the need for the NFC usage. 

.. code-block:: xml

    <key>NFCReaderUsageDescription</key>
    <string>Communication with NFC enabled eID cards for authentication processes</string>

Find a full example at the `reference implementation for ios <https://github.com/ecsec/open-ecard-ios>`_.


The corresponding protocol definitions of the API described in this document can be found within the bundle in the "Headers" folder.

Android
-------

The Open eCard App for Android consists of one JAR file.
The artifact is published in a maven repository which can be added to the gradle build as follows:

.. code-block:: Groovy

   repositories {
     maven {
       url "https://mvn.ecsec.de/repository/openecard-public"
     }
   }

The dependency must be defined as follows in the gradle file’s dependency section:

.. code-block:: Groovy
   :substitutions:

   implementation 'org.openecard.clients:android-lib:|release|'

Schema validation of the processed eCard API Messages is done with the Xerces XML Parser library, as the Android built-in library is not sufficient for this task:

.. code-block:: Groovy
  
  implementation 'xerces:xercesImpl:2.12.0'

Logging is performed with the `SLF4J API <https://www.slf4j.org/>`_.
In order to actually emit log messages, an implementation of the API such as `Logback <https://logback.qos.ch/>`_ or a wrapper for another logging system can be provided in the application.
Details can be found in the SLF4J or Logback documentation.
SLF4J must be added as a dependency, as it is not packed into the JAR file in order to not raise conflicts in case it is already used in the app.
Logback is optional in case no logging of the Open eCard Framework is needed:

.. code-block:: Groovy

  implementation 'org.slf4j:slf4j-api:1.7.26'
  implementation 'com.github.tony19:logback-android:1.3.0-2'

Due to the number of methods in the referenced libraries being greater than 65.536, MultiDex support must be enabled for Android apps which support an API level below 21.
The following statement has to be added to the gradle file:

.. code-block:: Groovy

  android {
    defaultConfig {
      multiDexEnabled true
    }
  }

Since the Open eCard Framework has been compiled for Java 8, the following entry is needed:

.. code-block:: Groovy

  compileOptions {
    sourceCompatibility 1.8
    targetCompatibility 1.8
  }

The minimum Android API version to run the Open eCard Framework is 21.
It is however possible to build an app with a lower API level.
In that case it must be checked manually, which API level the mobile supports before trying to initialize the Open eCard Framework.

The manifest file of the app using the Open eCard Framework must contain the following line to enable NFC and internet access of the device:

.. code-block:: xml

  <uses-permission android:name="android.permission.NFC" />
  <uses-permission android:name="android.permission.INTERNET" />
