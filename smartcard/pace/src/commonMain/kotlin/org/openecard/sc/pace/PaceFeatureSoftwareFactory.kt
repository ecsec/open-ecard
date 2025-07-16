package org.openecard.sc.pace

import org.openecard.sc.iface.CardChannel
import org.openecard.sc.iface.feature.PaceFeature
import org.openecard.sc.iface.feature.PaceFeatureFactory

class PaceFeatureSoftwareFactory : PaceFeatureFactory {
	@OptIn(ExperimentalUnsignedTypes::class)
	override fun create(
		channel: CardChannel,
		efCardAccess: UByteArray?,
	): PaceFeature = PaceProtocol(channel)
}
