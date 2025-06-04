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

class CardProtocolComparator : Comparator<CardProtocol> {
	override fun compare(
		a: CardProtocol,
		b: CardProtocol,
	): Int = a.ordinal().compareTo(b.ordinal())
}

fun CardProtocol.ordinal(): Int =
	when (this) {
		CardProtocol.Any -> 7
		CardProtocol.Grouped.CONTACT -> 5
		CardProtocol.Grouped.CONTACTLESS -> 6
		CardProtocol.Technical.T0 -> 1
		CardProtocol.Technical.T1 -> 2
		CardProtocol.Technical.T15 -> 3
		CardProtocol.Technical.TCL -> 4
	}
