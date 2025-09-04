package org.openecard.addons.tr03124.xml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("Result", prefix = Namespaces.DSS.PREFIX, namespace = Namespaces.DSS.NS)
data class Result(
	@SerialName("ResultMajor")
	@XmlElement
	val major: String,
	@SerialName("ResultMinor")
	@XmlElement
	val minor: String? = null,
	@XmlSerialName("ResultMessage")
	@XmlElement
	val message: InternationalString? = null,
)
