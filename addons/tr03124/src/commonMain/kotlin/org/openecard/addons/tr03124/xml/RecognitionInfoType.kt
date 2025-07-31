package org.openecard.addons.tr03124.xml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import org.openecard.utils.serialization.PrintableUByteArray
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
@XmlSerialName("RecognitionInfo", prefix = Namespaces.ISO.PREFIX, namespace = Namespaces.ISO.NS)
@OptIn(ExperimentalTime::class)
data class RecognitionInfoType(
	@SerialName("CardType")
	@XmlElement
	val cardType: String?,
	@SerialName("CardIdentifier")
	@XmlElement
	val cardIdentifier: PrintableUByteArray?,
	@SerialName("CaptureTime")
	@XmlElement
	val captureTime: Instant?,
)
