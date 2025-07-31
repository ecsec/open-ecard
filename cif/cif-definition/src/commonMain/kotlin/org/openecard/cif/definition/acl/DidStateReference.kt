package org.openecard.cif.definition.acl

import kotlinx.serialization.Serializable

@Serializable
data class DidStateReference(
	val name: String,
	val active: Boolean,
	/**
	 * State qualifier.
	 */
	val stateQualifier: AclQualifier?,
) : BoolTreeLeaf {
	fun matches(other: DidStateReference): Boolean {
		val reference = this
		val m1 = reference.name == other.name
		val m2 = reference.active == other.active
		val m3 =
			if (reference.stateQualifier == null) {
				// if reference does not mention a qualifier, we accept anything
				true
			} else {
				if (other.stateQualifier == null) {
					false
				} else {
					this.stateQualifier.matches(other.stateQualifier)
				}
			}
		return m1 && m2 && m3
	}

	companion object {
		fun forName(
			name: String,
			stateQualifier: AclQualifier? = null,
			active: Boolean = true,
		) = DidStateReference(name, active, stateQualifier)
	}
}
