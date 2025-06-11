package org.openecard.sc.apdu

class ApduProcessingError(
	val status: StatusWordResult,
	msg: String,
) : Exception(msg)
