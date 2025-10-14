package org.openecard.utils.common

fun <T> doIf(
	condition: Boolean,
	block: () -> T,
): T? =
	if (condition) {
		block()
	} else {
		null
	}

fun <E : Exception> throwIf(
	condition: Boolean,
	block: () -> E,
) {
	if (condition) {
		throw block()
	}
}

fun <T, E : Exception> throwIfNull(
	obj: T?,
	block: () -> E,
): T {
	if (obj == null) {
		throw block()
	} else {
		return obj
	}
}
