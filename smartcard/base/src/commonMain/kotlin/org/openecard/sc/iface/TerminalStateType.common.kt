package org.openecard.sc.iface

enum class TerminalStateType {
	/**
	 * There is no card in the reader.
	 */
	ABSENT,

	/**
	 * There is a card in the reader, but it has not been moved into position for use.
	 */
	PRESENT,

	/**
	 * There is a card in the reader in position for use. The card is not powered.
	 */
	SWALLOWED,

	/**
	 * Power is being provided to the card, but the reader driver is unaware of the mode of the card.
	 */
	POWERED,

	/**
	 * The card has been reset and is awaiting PTS negotiation.
	 */
	NEGOTIABLE,

	/**
	 * The card has been reset and specific communication protocols have been established.
	 */
	SPECIFIC,
}
