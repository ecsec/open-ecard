API Classes
===========

.. raw:: latex

   \begin{landscape}
   
.. _fig_api_classes:
.. figure:: img/api_classes.*
	   :width: 100%

	   API Classes

.. raw:: latex

   \end{landscape}


:numref:`fig_api_classes` shows a complete overview of the defined interfaces and data objects, which are used within a Custom App.

The package ``com.example.ios_app`` shows a stripped architecture of an app illustrating which interfaces have to be implemented within the custom app and provided to the Open eCard Framework.

The Confirmation operations which are given as parameters in the callback functions of the interactions, for example ``ConfirmPasswordOperation`` are left out of the diagram for simplicity.
They all provide one function to allow giving the needed PINs, CANs etc. to the framework.

With the ``minorProcessResult`` property of ``ActivationResult``, the ``ResultMinor`` as specified in [BSI_TR-03112-1]_ can be obtained.
