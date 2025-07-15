package org.openecard.sc.pace

import org.openecard.sc.iface.CardChannel
import org.openecard.sc.iface.feature.PaceFeature
import org.openecard.sc.iface.feature.PaceFeatureFactory

class PaceFeatureSoftwareFactory : PaceFeatureFactory {
	override fun create(channel: CardChannel): PaceFeature = PaceProtocol(channel)
}
