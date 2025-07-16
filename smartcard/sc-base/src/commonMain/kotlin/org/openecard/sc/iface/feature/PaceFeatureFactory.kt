package org.openecard.sc.iface.feature

import org.openecard.sc.iface.CardChannel

interface PaceFeatureFactory {
	@OptIn(ExperimentalUnsignedTypes::class)
	fun create(
		channel: CardChannel,
		efCardAccess: UByteArray? = null,
	): PaceFeature
}
