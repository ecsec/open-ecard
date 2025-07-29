package org.openecard.cif.dsl.builder

interface Builder<T> {
	fun build(): T
}
