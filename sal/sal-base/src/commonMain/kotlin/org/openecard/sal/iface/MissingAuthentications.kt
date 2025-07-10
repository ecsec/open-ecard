package org.openecard.sal.iface

import org.openecard.cif.definition.acl.BoolTreeAnd
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

	class MissingDidAuthentications(
		val decisions: BoolTreeOr<AclDidResolution>,
	) : MissingAuthentications {
		fun removeUnsupported(availableDids: List<DidStateReference>): MissingAuthentications {
			val orReduced =
				BoolTreeOr<AclDidResolution>(
					decisions.or.mapNotNull { ands ->
						val andReduced =
							ands.and.mapNotNull { term ->
								if (!availableDids.any { term.requiredState == it }) {
									// the caller can solve this DID, so keep it
									term
								} else {
									// unsolvable for the caller
									null
								}
							}
						if (andReduced.isEmpty()) {
							null
						} else {
							BoolTreeAnd(andReduced)
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
	}
}
