package org.openecard.cif.dsl.builder.did

import org.openecard.cif.definition.did.DidDefinition
import org.openecard.cif.dsl.api.CifMarker
import org.openecard.cif.dsl.api.did.DecryptionDidScope
import org.openecard.cif.dsl.api.did.DidDslScope
import org.openecard.cif.dsl.api.did.DidSetScope
import org.openecard.cif.dsl.api.did.EncryptionDidScope
import org.openecard.cif.dsl.api.did.SignatureDidScope
import org.openecard.cif.dsl.builder.Builder

class DidSetBuilder(
	val dids: MutableSet<DidDefinition> = mutableSetOf(),
) : DidSetScope,
	Builder<Set<DidDefinition>> {
	override fun pace(content: @CifMarker (DidDslScope.Pace.() -> Unit)) {
		val builder = PaceDidBuilder()
		content(builder)
		dids.add(builder.build())
	}

	override fun pin(content: @CifMarker (DidDslScope.Pin.() -> Unit)) {
		val builder = PinDidBuilder()
		content(builder)
		dids.add(builder.build())
	}

	override fun signature(content: @CifMarker (SignatureDidScope.() -> Unit)) {
		val builder = SignatureDidBuilder()
		content(builder)
		dids.add(builder.build())
	}

	override fun encrypt(content: @CifMarker (EncryptionDidScope.() -> Unit)) {
		val builder = EncryptionDidBuilder()
		content(builder)
		dids.add(builder.build())
	}

	override fun decrypt(content: @CifMarker (DecryptionDidScope.() -> Unit)) {
		val builder = DecryptionDidBuilder()
		content(builder)
		dids.add(builder.build())
	}

	override fun build(): Set<DidDefinition> = dids
}
