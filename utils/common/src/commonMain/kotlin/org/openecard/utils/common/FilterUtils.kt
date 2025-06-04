package org.openecard.utils.common

fun <T> T.returnIf(block: (T) -> Boolean): T? =
	if (block(this)) {
		this
	} else {
		null
	}

fun <T> T.nullIf(block: (T) -> Boolean): T? = returnIf { !block(it) }
