Basic Architecture
==================

.. _fig_basic_arch:

.. figure:: img/basic_arch.*
	   :width: 100%

	   Basic architecture

As outlined in :numref:`fig_basic_arch`, a Custom App integrating the Open eCard Framework mainly makes use of two defined interfaces.
To use them a custom app has to provide implementations of callbacks enabling the framework to interact with users, like asking for the PIN etc.
That way the way the user gets asked can be designed freely.
The corresponding callbacks are used by the framework to accomplish processes with identity cards.
The Custom App does not have to deal with the flow control of these processes which is completely encapsulated in the Open eCard Framework.
The ``PinManagementController`` is used for PIN related functionality like changing the PIN or unlock it with the PUK.
The ``EacController`` provides all functionality needed for authentication and access control processes (EAC).
Details of both will be described later in this document.
