.. _sec_pin_management:

PinManagement
=============

.. _fig_pin_classes:
.. figure:: img/pin_classes.*
	   :width: 100%

	   PinManagement related classes

The PIN management process follows the same pattern as the EAC process described in :numref:`sec_eac_controller`.
The difference is that in this case an implementation of the ``PinManagementInteraction`` has to be provided to the Open eCard Framework.
The framework will again call the methods if needed during the process.
