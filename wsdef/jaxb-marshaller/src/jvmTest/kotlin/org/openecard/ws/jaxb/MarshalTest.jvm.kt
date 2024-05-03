/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 */
package org.openecard.ws.jaxb

import de.bund.bsi.ecard.api._1.InitializeFrameworkResponse
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticateResponse
import iso.std.iso_iec._24727.tech.schema.EAC2OutputType
import oasis.names.tc.dss._1_0.core.schema.InternationalStringType
import oasis.names.tc.dss._1_0.core.schema.Result
import org.openecard.ws.marshal.MarshallingTypeException
import org.openecard.ws.soap.MessageFactory
import org.openecard.ws.soap.SOAPException
import org.testng.Assert
import org.testng.annotations.Test
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.math.BigInteger
import javax.xml.namespace.QName
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.TransformerException

/**
 *
 * @author Tobias Wich
 */
class MarshalTest {
    var xmlStr = """<?xml version="1.0" encoding="UTF-8"?>
<soap11:Envelope xmlns:soap11="http://schemas.xmlsoap.org/soap/envelope/" xmlns:addr="http://www.w3.org/2005/03/addressing" xmlns:paos20="urn:liberty:paos:2006-08">
  <soap11:Header>
    <paos20:PAOS soap11:actor="http://schemas.xmlsoap.org/soap/actor/next" soap11:mustUnderstand="1">
      <paos20:Version>urn:liberty:paos:2006-08</paos20:Version>
    </paos20:PAOS>
    <MessageID xmlns="http://www.w3.org/2005/03/addressing">urn:uuid:00dcda36-bc0b-11df-b497-0a0027000000</MessageID>
  </soap11:Header>
  <soap11:Body>
    <ns3:StartPAOS xmlns:ns3="urn:iso:std:iso-iec:24727:tech:schema"
                   xmlns="urn:oasis:names:tc:dss:1.0:core:schema"
                   xmlns:ns2="http://www.w3.org/2000/09/xmldsig#"
                   xmlns:ns4="http://ws.gematik.de/fa/vods/eVerordnungXML/v6.0"
                   xmlns:ns5="http://www.w3.org/2001/04/xmlenc#"
                   xmlns:ns6="http://ws.gematik.de/fa/vods/TransportPayload/v6.0"
                   xmlns:ns7="http://ws.gematik.de/fa/vods/eDispensierungXML/v6.0"
                   xmlns:ns8="http://ws.epotheke.com/bs/v1.0" Profile="http://ws.ecsec.de">
      <ns3:SessionIdentifier>0123456789</ns3:SessionIdentifier>
      <ns3:ConnectionHandle>
        <ns3:ContextHandle>A5E007BE249B4A747C6BD0CFCAD584D9</ns3:ContextHandle>
        <ns3:IFDName>SCM SCR 3311 [CCID Interface] (21120940200534) 00 00</ns3:IFDName>
        <ns3:SlotHandle>3C78847D2A6BCD56B7CB320FB92960F1D68C04A1</ns3:SlotHandle>
        <ns3:RecognitionInfo>
          <ns3:CardType>http://ws.gematik.de/egk/1.0.0</ns3:CardType>
          <ns3:CardIdentifier>80276883111211400934</ns3:CardIdentifier>
          <ns3:CaptureTime>2010-09-09T14:08:16.720+02:00</ns3:CaptureTime>
        </ns3:RecognitionInfo>
      </ns3:ConnectionHandle>
      <ns3:ConnectionHandle>
        <ns3:ContextHandle>A5E007BE249B4A747C6BD0CFCAD584D9</ns3:ContextHandle>
        <ns3:IFDName>SCM SCR 3311 [CCID Interface] (21120940200534) 00 00</ns3:IFDName>
        <ns3:SlotHandle>CF7A39B71D9D85D211B231B7A082485BDAAF9F96</ns3:SlotHandle>
        <ns3:RecognitionInfo>
          <ns3:CardType>http://ws.gematik.de/egk/1.0.0</ns3:CardType>
          <ns3:CardIdentifier>80276883111211400934</ns3:CardIdentifier>
          <ns3:CaptureTime>2010-09-09T14:08:47.942+02:00</ns3:CaptureTime>
        </ns3:RecognitionInfo>
      </ns3:ConnectionHandle>
    </ns3:StartPAOS>
  </soap11:Body>
</soap11:Envelope>"""

    @Test
    @Throws(Exception::class)
    fun testConversion() {
        val m = JAXBMarshaller()
        var doc = m.str2doc(xmlStr)

        val msg = m.doc2soap(doc)
        val body = msg.soapBody
        val result: Node = body.childElements[0]

        //System.out.println(m.doc2str(result));
        val o = m.unmarshal(result)
        doc = m.marshal(o)
        Assert.assertNotNull(doc)
        //System.out.println(m.doc2str(doc));
    }

    @Test
    @Throws(Exception::class)
    fun testSOAPMarshal() {
        val m = JAXBMarshaller()
        var doc = m.str2doc(xmlStr)
        val msg = m.doc2soap(doc)

        val o = m.unmarshal(msg.soapBody.childElements[0])
        doc = m.marshal(o)

        val factory = MessageFactory.newInstance()
        val soapMsg = factory.createMessage()
        soapMsg.soapBody.addDocument(doc)
        //soapMsg.writeTo(System.out);
    }

    @Test
    @Throws(
        MarshallingTypeException::class,
        TransformerException::class,
        SOAPException::class,
        ParserConfigurationException::class
    )
    fun testConversionOfDIDAuthenticateResponseAndInitializeFrameworkResponse() {
        val m = JAXBMarshaller()

        val didAuthenticateResponse = DIDAuthenticateResponse()
        val r = Result()
        r.resultMajor = "major"
        r.resultMinor = "minor"
        val internationalStringType = InternationalStringType()
        internationalStringType.lang = "en"
        internationalStringType.value = "message"
        r.resultMessage = internationalStringType
        didAuthenticateResponse.result = r

        val didAuthenticationDataType = EAC2OutputType()

        val factory = DocumentBuilderFactory.newInstance()
        factory.isNamespaceAware = true
        val builder = factory.newDocumentBuilder()
        val d = builder.newDocument()

        val e = d.createElementNS("urn:iso:std:iso-iec:24727:tech:schema", "Signature")
        e.textContent =
            "7117D7BF95D8D6BD437A0D43DE48F42528273A98F2605758D6A3A2BFC38141E7577CABB4F8FBC8DF152E3A097D1B3A703597331842425FE4A9D0F1C9067AC4A9"
        didAuthenticationDataType.any.add(e)

        didAuthenticateResponse.authenticationProtocolData = didAuthenticationDataType

        var doc = m.marshal(didAuthenticateResponse)

        println(m.doc2str(doc)) //test ok if this works

        val initializeFrameworkResponse = InitializeFrameworkResponse()
        val version = InitializeFrameworkResponse.Version()
        version.major = BigInteger("11")
        version.minor = BigInteger("22")
        version.subMinor = BigInteger("33")

        initializeFrameworkResponse.version = version

        r.resultMessage = internationalStringType
        initializeFrameworkResponse.result = r

        doc = m.marshal(initializeFrameworkResponse)

        println(m.doc2str(doc)) //test ok if this works
    }

    @Test
    @Throws(Exception::class)
    fun testSoapHeaderAdd() {
        val m = JAXBMarshaller()
        val doc = m.str2doc(xmlStr)
        val msg = m.doc2soap(doc)

        var msgId: Element? = null
        // check if messageid is present
        for (next in msg.soapHeader.childElements) {
            if (next.nodeName == "MessageID" && next.namespaceURI == "http://www.w3.org/2005/03/addressing") {
                msgId = next
            }
        }
        Assert.assertNotNull(msgId)

        // add relates to
        val relates = msg.soapHeader.addHeaderElement(QName("http://www.w3.org/2005/03/addressing", "RelatesTo"))
        relates.textContent = "relates to fancy id"

        println(m.doc2str(msg.document))
    }
}
