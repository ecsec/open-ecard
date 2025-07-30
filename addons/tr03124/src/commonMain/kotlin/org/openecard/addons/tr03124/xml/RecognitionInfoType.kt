package org.openecard.addons.tr03124.xml

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import org.openecard.utils.serialization.PrintableUByteArray

@Serializable
@XmlSerialName("RecognitionInfo", prefix = Namespaces.ISO.PREFIX, namespace = Namespaces.ISO.NS)
class RecognitionInfoType(
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
