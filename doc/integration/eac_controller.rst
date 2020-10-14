.. _sec_eac_controller:

EacController
=============

.. _fig_eac_classes:
.. figure:: img/eac_classes.*
	   :width: 100%

	   EAC related classes

The ``EacControllerFactory`` provides the method ``create()``, which starts the corresponding process and returns an ``ActivationController`` that allows, for example, to cancel the process.

As first parameter the service URL for which EAC should be performed, is set.

Secondly an implementation of the ``ControllerCallback`` interface has to be provided.
Its callback method gets invoked when the process ends and the ``ActivationResult`` is provided there.

As third argument an implementation of the ``EacInteraction`` is expected.
This implementation has to provide implementations of all needed methods to be able to complete an EAC interaction.
The methods will be called by the Open eCard Framework when needed and at the correct point in the process flow.
That way the responsibility to control the process is completely encapsulated in the Open eCard Framework and the Custom App has no need to bother with it.

iOS URLEncoding helper
----------------------

The tcToken URL which is given to the framework as parameter in the service URL has to be properly URL-encoded.
To accomplish that in a simple way the ``OpeneCard`` object described earlier provides the function ``prepareTCTokenURL()``.
