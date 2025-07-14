package org.openecard.sc.iface.feature

import org.openecard.sc.iface.CardChannel

interface PaceFeatureFactory {
	fun create(channel: CardChannel): PaceFeature
}
