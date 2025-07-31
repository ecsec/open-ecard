package org.openecard.addons.tr03124.xml

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("InitializeFramework", prefix = Namespaces.ISO.PREFIX, namespace = Namespaces.ISO.NS)
data class InitializeFrameworkRequest(
	override val requestId: String?,
	override val profile: String? = ECardConstants.Profile.ECARD_1_1,
) : RequestType
