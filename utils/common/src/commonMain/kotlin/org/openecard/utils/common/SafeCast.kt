package org.openecard.utils.common

inline fun <reified T> Any.cast(): T? =
	when (this) {
		is T -> this
		else -> null
	}
