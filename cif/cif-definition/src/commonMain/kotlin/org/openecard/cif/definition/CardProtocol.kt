package org.openecard.cif.definition

sealed interface CardProtocol {
	enum class Technical : CardProtocol {
		T0,
		T1,
		T15,
		TCL,
	}

	enum class Grouped : CardProtocol {
		CONTACT,
		CONTACTLESS,
	}

	object Any : CardProtocol
}
