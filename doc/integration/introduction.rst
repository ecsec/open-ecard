.. _sec_introduction:

Introduction
============

.. _fig_eid_activation:
.. figure:: img/eid-activation.*
	   :width: 100%

	   eID-Activation according to BSI TR-03124-1

The "*Open eCard Framework*" is an *eID-Kernel* according to [BSI_TR-03124-1]_, which is written in Java and allows building *eID-Clients*, such as the "Open eCard App". 

The technical guideline [BSI_TR-03124-1]_ defines a mode of operation for eID-Clients [#f1]_ which allows external applications to start the eID process via specially registered URLs.
The Open eCard Framework supports the following base URLs according to the technical guideline:

* http://127.0.0.1:24727/eID-Client
* http://localhost:24727/eID-Client

The following URLs should be supported additionally on mobile systems:

* eid://127.0.0.1:24727/eID-Client
* eid://localhost:24727/eID-Client

It should be noted that theoretically the hostname "localhost" could point to a different IP than 127.0.0.1, thus it is recommended to use the first URLs with the explicit IP declaration.

The general process of the eID-Activation is shown in :numref:`fig_eid_activation`.


.. rubric:: Footnotes

.. [#f1] This does not apply to so called "Integrated eID-Clients", which do not offer the eID-functionality to other applications on the mobile device.
