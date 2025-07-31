package org.openecard.cif.bundled

import org.openecard.cif.definition.meta.CardInfoStatus
import org.openecard.cif.dsl.builder.CardInfoBuilder
import org.openecard.cif.dsl.builder.unaryPlus
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalUnsignedTypes::class, ExperimentalTime::class)
val HpcQSigCif by lazy {
	val b = CardInfoBuilder()

	b.metadata {
		id = "http://www.dgn.de/cif/HPCqSIG"
		version = "1.0.0"
		status = CardInfoStatus.DEVELOPMENT
		name = "HPCqSIG"
		cardIssuer = "DGN Deutsches Gesundheitsnetz"
		creationDate = Instant.parse("2025-06-25T00:00:00Z")
		modificationDate = Instant.parse("2025-06-25T00:00:00Z")
	}

	b.build()
}
