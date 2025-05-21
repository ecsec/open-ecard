package org.openecard.sal.iface

interface BoolTreeOr<T> {
	val or: List<BoolTreeAnd<T>>
}

interface BoolTreeAnd<T> {
	val and: List<T>
}
