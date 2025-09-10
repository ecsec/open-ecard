package org.openecard.addons.tr03124.xml

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

// The PAOS specification vanished from the internet.
// Here is the definition of the PAOS Element for reference purpose.
//
// <xs:element name="PAOS" type="PaosType"/>
// <xs:complexType name="PaosType">
//   <xs:complexContent>
//     <xs:attribute ref="S:mustUnderstand" use="required"/>
//     <xs:attribute ref="S:actor" use="required"/>
//     <xs:sequence>
//       <xs:element name="Version" type="xs:anyURI" minOccurs="1" maxOccurs="unbounded"/>
//       <xs:element ref="a:EndpointReference" minOccurs="0" maxOccurs="unbounded"/>
//       <xs:any namespace="##other" processContents="lax" minOccurs="0" maxOccurs="unbounded"/>
//     </xs:sequence>
//   </xs:simpleContent>
// </xs:complexType>

@Serializable
@XmlSerialName("PAOS", prefix = Namespaces.PAOS.PREFIX, namespace = Namespaces.PAOS.NS)
data class Paos(
	// TODO: make sure the value is marshalled to 0 or 1, not false or true
	@XmlSerialName("mustUnderstand", namespace = Namespaces.SOAP.NS, prefix = Namespaces.SOAP.PREFIX)
	@XmlElement(false)
	val mustUnderstand: Boolean,
	@XmlSerialName("actor", namespace = Namespaces.SOAP.NS, prefix = Namespaces.SOAP.PREFIX)
	@XmlElement(false)
	val actor: String,
	@SerialName("Version")
	@XmlElement
	val version: List<String> = emptyList(),
	@XmlElement
	val endpointReference: List<EndpointReference> = emptyList(),
)
