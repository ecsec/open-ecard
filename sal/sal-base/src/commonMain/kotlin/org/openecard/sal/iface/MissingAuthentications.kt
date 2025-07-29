package org.openecard.sal.iface

import org.openecard.cif.definition.acl.BoolTreeOr
import org.openecard.cif.definition.acl.DidStateReference

sealed interface MissingAuthentications {
	@Throws(AclUnfulfillable::class)
	fun missingDidObjectOrThrow(): MissingDidAuthentications {
		when (this) {
			is MissingDidAuthentications -> return this
			Unsolveable -> throw AclUnfulfillable()
		}
	}

	val isSolved: Boolean

	fun removeUnsupported(predicate: (AclDidResolution) -> Boolean): MissingAuthentications

	fun removeUnsupported(availableDids: List<DidStateReference>): MissingAuthentications =
		removeUnsupported { term -> availableDids.any { did -> term.requiredState == did } }

	class MissingDidAuthentications(
		internal val decisions: BoolTreeOr<AclDidResolution>,
	) : MissingAuthentications {
		val options = decisions.or.map { it.and }

		override fun removeUnsupported(predicate: (AclDidResolution) -> Boolean): MissingAuthentications {
			val orReduced =
				BoolTreeOr<AclDidResolution>(
					decisions.or.mapNotNull { ands ->
						val allDidsSupported = ands.and.all { term -> predicate(term) }
						if (allDidsSupported) {
							ands
						} else {
							null
						}
					},
				)

			return if (orReduced.or.isEmpty()) {
				Unsolveable
			} else {
				MissingDidAuthentications(orReduced)
			}
		}

		override val isSolved: Boolean
			get() = decisions.or.isEmpty()
	}

	object Unsolveable : MissingAuthentications {
		override val isSolved: Boolean = false

		override fun removeUnsupported(predicate: (AclDidResolution) -> Boolean): MissingAuthentications = this
	}
}
