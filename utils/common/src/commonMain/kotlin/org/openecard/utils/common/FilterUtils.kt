package org.openecard.utils.common

fun <T> T.returnIf(block: (T) -> Boolean): T? =
	if (block(this)) {
		this
	} else {
		null
	}

fun <T> T.nullIf(block: (T) -> Boolean): T? = returnIf { !block(it) }

fun <T> doIf(
	condition: Boolean,
	block: () -> T,
): T? =
	if (condition) {
		block()
	} else {
		null
	}
