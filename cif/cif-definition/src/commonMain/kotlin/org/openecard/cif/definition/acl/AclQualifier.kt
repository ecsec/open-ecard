package org.openecard.cif.definition.acl

sealed interface AclQualifier {
	fun matches(other: AclQualifier): Boolean
}
