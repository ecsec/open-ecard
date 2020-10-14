Example of an EAC process
=========================

.. _fig_eac_flow:
.. figure:: img/eac_flow.*
	   :width: 100%

	   EAC flow example

:numref:`fig_eac_flow` shows an example flow of an EAC process.
It begins with creating an instance of ``EacInteraction`` and calling the ``create()`` method of the ``EacControllerFactory`` with a reference to it.
The creation of the ``ControllerCallback`` is omitted for simplicity.
With the call of ``create()`` the process control for the EAC process is handed to the Open eCard Framework.

The methods of ``EacInteraction`` have to be implemented in a functional and stateless way, meaning the functions should work independently from each other, since the Open eCard Framework decides when the single functions are called.

``onServerData()`` provides the app with the server data the service requests, which the app has to show to the user.
In addition, a callback is given where the attributes a user allows to provide to the service have to be given to the framework.
Once the callback has been invoked with this data the process continues.

``onPinRequest()`` works in a similar way and after the user entered a PIN the provided callback has to be invoked with it.

``requestCardInsertion()`` informing the custom app implementation that a card should be provided by the user.

``onCardRecognized()`` informs the custom app when a card was detected by the nfc reader.

``onCardAuthenticationSuccessful()`` informs the EacInteraction object that interaction with the card finished successfully.

``onAuthenticationCompletion()`` is used to inform the custom app via the ``ControllerCallback`` object, created in the beginning, when the whole process is over.

The shown flow is an example and might vary depending on circumstances, which is not shown in the diagram.
For example, the given PIN might be incorrect.
This case would be handled by a new call to ``onPinRequest()`` if the PIN is not yet blocked.
The latter case would result in a call to ``onPinBlocked()``.

There are two variants of ``onPinRequest()``, one with a parameter attempt and one without.
If the framework already had contact to a card and was able to read the PIN state of it, the callback with attempt parameter is used providing the number of attempts left to authenticate with PIN.
This enables the custom app to inform the user about the number of remaining attempts.
The number denotes the number of attempts including the one which is requested by invoking the callback, meaning, if the value is 3, entering a wrong PIN would result in a second call with a value of 2.
Note that the last attempt also requires the CAN in which case the framework calls ``onPinCanRequest()``, where no attempt count is explicitly given since it is the last attempt before PUK is needed to unblock the card.

Note, that the whole process is designed in an asynchronous way, letting the Custom App run in parallel to the EAC process controlled by the Open eCard Framework.
However it should be stated that on iOS NFC reader sessions take place in the foreground of the device and have the exclusive focus during communication with smart cards.

Also it should be noted that the process begins with the PIN request before the card is requested.
This allows the framework to perform the whole process without interruptions to ask the user for the PIN like the earlier version.
This behaviour was likely to fail, since the user often moves card and device during typing, which lead to connection losses and failures in versions 1.x of the Open eCard Framework.
However if the PIN is wrong, it gets asked a second time.
This is now also handled more reliable since version 2.0.0 decouples the user interaction completely from the technical communication with the card and thus the framework can reconnect to the card while the callbacks are pending.
