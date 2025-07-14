import org.openecard.sc.iface.CardChannel
import org.openecard.sc.iface.feature.PaceFeature
import org.openecard.sc.iface.feature.PaceFeatureFactory
import org.openecard.sc.pace.PaceProtocol

class PaceFeatureSoftwareFactory : PaceFeatureFactory {
	override fun create(channel: CardChannel): PaceFeature = PaceProtocol(channel)
}
