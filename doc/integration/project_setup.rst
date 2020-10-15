Project Setup
=============

iOS
---

The Open eCard Framework for iOS is delivered as a Swift and ObjC compatible framework which can be added to a project in XCode.

The framework must be "embedded and signed" during the build phase of XCode.

Within "Capabilities and Signing" the "Near field communication" capability has to be activated.

The corresponding protocol definitions of the API described in this document can be found within the bundle in the "Headers" folder.

Android
-------

The Open eCard App for Android consists of one aar packed module.
The dependency must be defined as follows in the gradle file’s dependency section:

.. code-block:: Groovy

   implementation(group: 'org.openecard.clients', name: 'android-lib', version: "${OecVersion}")

The version may be defined at the top level with the following statement naming the correct version, for example:

.. code-block:: Groovy
   :substitutions:
      
   def OecVersion = '|release|'

Schema validation of the processed eCard API Messages is done with the Xerces XML Parser library, as the Android built-in library is not sufficient for this task:

.. code-block:: Groovy
  
  implementation 'xerces:xercesImpl:2.12.0'

Logging is performed with the `SLF4J API <https://www.slf4j.org/>`_.
In order to actually emit log messages, an implementation of the API such as `Logback <https://logback.qos.ch/>`_ or a wrapper for another logging system can be provided in the application.
Details can be found in the SLF4J or Logback documentation.
SLF4J must be added as a dependency, as it is not packed into the aar in order to not raise conflicts in case it is already used in the app.
Logback is optional in case no logging of the Open eCard Framework is needed:

.. code-block:: Groovy

  implementation 'org.slf4j:slf4j-api:1.7.25'
  implementation 'com.github.tony19:logback-android:1.1.1-12'

Due to the high number of methods (> 65.536) in the referenced libraries, MultiDex support must be enabled for Android apps which support an API level below 21.
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
The manifest file of the app using the Open eCard Framework must contain the following line to enable the NFC functionalities of the device:

.. code-block:: xml

  <uses-permission android:name="android.permission.NFC" />