Integration and Initialisation
==============================

To properly integrate the Open eCard Framework into a Custom App, it should be understood that the Open eCard Framework offers an object-oriented API.
The functionality encapsulates asynchronous processes and offers event-driven notifications to transparently notify the most current state of the ongoing process.
The solution does not use an observer-subscriber pattern.
Instead, the Custom App initiates each asynchronous process with its own implementation of an Open eCard Framework interface.
At predetermined stages or events in the asynchronous process, the Open eCard Framework calls specific methods on this implementation.

Additionally, the Open eCard Framework supports context-specific interactions by passing in additional process specific callbacks.

.. _fig_bootstrap:
.. figure:: img/bootstrap.*
	   :width: 100%

	   Bootstrap

:numref:`fig_bootstrap` shows the application-specific classes ``android.OpeneCard`` and ``ios.Openecard`` both providing a static function ``createInstance()`` allowing the custom App to optain an instance of the framework.
Both define the function: ``context()`` which returns a ``ContextManager`` object.

``ContextManager`` can be used to start and stop the framework and its background processes.
The methods ``initializeContext()`` and ``terminateContext()`` both require callback handlers providing functionality handling success or failure of these calls.

On a successful call to ``initializeContext()``, the ``onSuccess()`` callback of ``StartServiceHandler`` is invoked with an instance of ``ActivationSource``.
The latter can then be used to get factories enabling the Custom App to perform the two main processes EAC and PIN management (see Sections :numref:`{number} <sec_eac_controller>` and :numref:`{number}  <sec_pin_management>`.

iOS
---

Within a Custom App on iOS, the static function ::

   static OpenEcard* createInstance()

has to be called to gather an instance of the Open eCard Framework.
More specifically an instance of ``OpeneCard``, which allows creating the ``ContextManager`` described above.

Android
-------

Within an activity of an Android app, one has to call the static function ``org.openecard.android.activation.OpeneCard.createInstance()`` to create and obtain a reference to the framework.
The obtained object allows to call the ``context(Activity)`` method which needs a reference to the calling activity and returns a context object allowing to start the Open eCard framework services.
The ``AndroidContextManager`` object contains the method: ``onNewNfcIntent(Intent)`` which has to be used to inject the ``NfcIntent`` into the Open eCard Framework which was passed to the app via ``Activity.onNewIntent(Intent)``.
