package org.openecard.cif.bundled

import org.openecard.cif.definition.meta.CardInfoStatus
import org.openecard.cif.dsl.builder.CardInfoBuilder
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalUnsignedTypes::class, ExperimentalTime::class)
val EaLightCif by lazy {
	val b = CardInfoBuilder()

	b.metadata {
		id = "http://www.aekno.de/eAT-light"
		version = "1.0.0"
		status = CardInfoStatus.DEVELOPMENT
		name = "eArztausweis Light (V.03)"
		creationDate = Instant.parse("2025-06-25T00:00:00Z")
		modificationDate = Instant.parse("2025-06-25T00:00:00Z")
	}

	b.build()
}
