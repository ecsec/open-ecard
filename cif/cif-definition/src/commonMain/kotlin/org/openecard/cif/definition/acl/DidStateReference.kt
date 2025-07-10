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
		val m1 = this.name == other.name
		val m2 = this.active == other.active
		val m3 =
			if (this.stateQualifier == null && other.stateQualifier == null) {
				true
			} else if (this.stateQualifier != null && other.stateQualifier != null) {
				this.stateQualifier.matches(other.stateQualifier)
			} else {
				false
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
