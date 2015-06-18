/****************************************************************************
 * Copyright (C) 2012-2015 HS Coburg.
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
 ***************************************************************************/

package org.openecard.crypto.common.sal;

import iso.std.iso_iec._24727.tech.schema.AlgorithmIdentifierType;
import iso.std.iso_iec._24727.tech.schema.AlgorithmInfoType;
import iso.std.iso_iec._24727.tech.schema.CardCallTemplateType;
import iso.std.iso_iec._24727.tech.schema.CertificateRefType;
import iso.std.iso_iec._24727.tech.schema.CryptoKeyInfoType;
import iso.std.iso_iec._24727.tech.schema.DIDAbstractMarkerType;
import iso.std.iso_iec._24727.tech.schema.HashGenerationInfoType;
import iso.std.iso_iec._24727.tech.schema.KeyRefType;
import iso.std.iso_iec._24727.tech.schema.StateInfo;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.openecard.common.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * The class implements a CryptoMarkerType object according to BSI TR-0312 part 7.
 *
 * @author Dirk Petrautzki
 * @author Hans-Martin Haase
 */
public class CryptoMarkerType {

    private String legacyKeyName = null;
    private AlgorithmInfoType algorithmInfo = null;
    private HashGenerationInfoType hashGenerationInfo = null;
    private List<CertificateRefType> certificateRefs = null;
    private CryptoKeyInfoType cryptoKeyInfo = null;
    private String[] signatureGenerationInfo = null;
    private List<CardCallTemplateType> legacySignatureGenerationInfo = null;
    private final String protocol;

    /**
     * The constructor gets an {@link DIDAbstractMarkerType} object and parses the object to a CryptoMarkerType object.
     * The CryptoMarkerType object is based on the CryptoMarkerType from BSI TR-03112-7
     *
     * @param baseType a {@link DIDAbstractMarkerType} object to parse.
     */
    public CryptoMarkerType(iso.std.iso_iec._24727.tech.schema.DIDAbstractMarkerType baseType) {
	protocol = baseType.getProtocol();
	for (Element elem : baseType.getAny()) {
	    switch (elem.getLocalName()) {
	    	case "AlgorithmInfo":
		    algorithmInfo = new AlgorithmInfoType();
		    NodeList algorithmInfoNodes = elem.getChildNodes();
		    for (int i = 0; i < algorithmInfoNodes.getLength(); i++) {
			Node node = algorithmInfoNodes.item(i);
		switch (node.getLocalName()) {
		    case "Algorithm":
			algorithmInfo.setAlgorithm(node.getTextContent());
			break;
		    case "AlgorithmIdentifier":
			AlgorithmIdentifierType algorithmIdentifierType = new AlgorithmIdentifierType();
			NodeList nodeList = node.getChildNodes();
			for (int y = 0; y < nodeList.getLength(); y++) {
			    Node n = nodeList.item(y);
			    if (null != n.getLocalName()) switch (n.getLocalName()) {
			    	case "Algorithm":
				    algorithmIdentifierType.setAlgorithm(n.getTextContent());
				    break;
			    	case "Parameters":
				    algorithmIdentifierType.setParameters(n);
				    break;
			    }
			}
			algorithmInfo.setAlgorithmIdentifier(algorithmIdentifierType);
			break;
		    case "SupportedOperations":
			String[] supportedOperations = node.getTextContent().split(" ");
			algorithmInfo.getSupportedOperations().addAll(Arrays.asList(supportedOperations));
			break;
		    case "CardAlgRef":
			algorithmInfo.setCardAlgRef(StringUtils.toByteArray(node.getTextContent()));
			break;
		    case "HashAlgRef":
			algorithmInfo.setHashAlgRef(StringUtils.toByteArray(node.getTextContent()));
			break;
		}
		    }   break;
		case "KeyInfo":
		{
		    cryptoKeyInfo = new CryptoKeyInfoType();
		    NodeList nodeList = elem.getChildNodes();
		    for (int i = 0; i < nodeList.getLength(); i++) {
			Node n = nodeList.item(i);
			switch (n.getLocalName()) {
			    case "KeyRef":
				KeyRefType keyRef = new KeyRefType();
				keyRef.setKeyRef(StringUtils.toByteArray(n.getTextContent()));
				cryptoKeyInfo.setKeyRef(keyRef);
				break;
			    case "KeySize":
				cryptoKeyInfo.setKeySize(new BigInteger(n.getTextContent()));
				break;
			}
		}	break;
		    }
		case "SignatureGenerationInfo":
		    signatureGenerationInfo = elem.getTextContent().split(" ");
		    break;
		case "LegacySignatureGenerationInfo":
		{
		    NodeList nodeList = elem.getChildNodes();
		    legacySignatureGenerationInfo = new ArrayList<>();
		    for (int i = 0; i < nodeList.getLength(); i++) {
			Node n = nodeList.item(i);
			if (n.getLocalName().equals("CardCommand")) {
			    NodeList nodeList2 = n.getChildNodes();
			    CardCallTemplateType cctt = new CardCallTemplateType();
			    for (int j = 0; j < nodeList2.getLength(); j++) {
				Node n2 = nodeList2.item(j);
				String localName = n2.getLocalName();
				switch (localName) {
				    case "HeaderTemplate":
					cctt.setHeaderTemplate(n2.getTextContent());
					break;
				    case "DataTemplate":
					cctt.setDataTemplate(n2.getTextContent());
					break;
				    case "ExpectedLength":
					cctt.setExpectedLength(BigInteger.valueOf(Integer.parseInt(n2.getTextContent())));
					break;
				}
			    }
			    legacySignatureGenerationInfo.add(cctt);
		    }
		}	break;
		    }
		case "HashGenerationInfo":
		    hashGenerationInfo = HashGenerationInfoType.fromValue(elem.getTextContent());
		    break;
		case "CertificateRef":
		{
		    if (certificateRefs == null) {
			certificateRefs = new ArrayList<>();
		    }	CertificateRefType certificateRef = new CertificateRefType();
		    NodeList nodeList = elem.getChildNodes();
		    for (int i = 0; i < nodeList.getLength(); i++) {
			Node n = nodeList.item(i);
			switch (n.getLocalName()) {
			    case "DataSetName":
				certificateRef.setDataSetName(n.getTextContent());
				break;
			    case "DSIName":
				certificateRef.setDSIName(n.getTextContent());
				break;
			    case "CertificateType":
				certificateRef.setCertificateType(n.getTextContent());
				break;
			}
		}	certificateRefs.add(certificateRef);
			break;
		    }
		case "LegacyKeyName":
		    this.legacyKeyName = elem.getTextContent();
		    break;
	    	case "StateInfo":
		    break;
	    }
	}
    }

    /**
     * Get the value of the property SignatureGenerationInfo if it exists.
     *
     * @return An array containing predefined strings from BSI TR-03112-7 page 82 SignatureGenerationInfo.  If no such
     * information is available NULL is returned.
     */
    public String[] getSignatureGenerationInfo() {
	if (signatureGenerationInfo == null) {
	    return null;
	}
	return signatureGenerationInfo.clone();
    }

    /**
     * Get the value of the property LegacySignatureGenerationIndo if it exists.
     *
     * @return A list of {@link CardCallTemplateType} objects which contain specific APDUs to generate a signature with
     *   the currently used card. If no such information is available {@code null} is returned.
     */
    public List<CardCallTemplateType> getLegacySignatureGenerationInfo() {
	if (legacySignatureGenerationInfo == null) {
	    return null;
	}
	return Collections.unmodifiableList(legacySignatureGenerationInfo);
    }

    /**
     * Get the value of the property cryptoKeyInfo if it exists.
     *
     * @return A {@link CryptoKeyInfoType} object which contains all known information about a key. If no such
     *   information is available {@code null} is returned.
     */
    public CryptoKeyInfoType getCryptoKeyInfo() {
	return cryptoKeyInfo;
    }

    /**
     * Get the value of the property LegacyKeyName if it exists.
     *
     * @return A string containing a key name. If no such key name is available {@code null} is returned.
     */
    public String getLegacyKeyName() {
	return legacyKeyName;
    }

    /**
     * Get the value of the property AlgorithmInfo if it exists.
     *
     * @return An {@link AlgorithmInfoType} object which contains all information available about a key. If no such
     * information exists NULL is returned.
     */
    public AlgorithmInfoType getAlgorithmInfo() {
	return algorithmInfo;
    }

    /**
     * Get the value of the property HashGenetationInfo if it exists.
     *
     * @return A {@link HashGenerationInfoType} object containing all necessary information about the creation of a
     *   hash. If no such information is available {@code null} is returned.
     */
    public HashGenerationInfoType getHashGenerationInfo() {
	return hashGenerationInfo;
    }

    /**
     * Get the value of the property CertificateRefs if it exists.
     * Per convention the first certificate in the list is the one to use for TLS authentication or signature creation
     * all other certificates are part of the certificate chain for the validation.
     *
     * @return A list of {@link CertificateRefType} object which contains references to a certificate object and the
     *   possible chain. If no such object exists {@code null} is returned.
     */
    public List<CertificateRefType> getCertificateRefs() {
	return certificateRefs;
    }

    /**
     * Get the value of the property StateInfo if it exists.
     * <br><br>
     * NOTE: This method is currently not implemented and throws an UnsupportedOperationException if the method is
     * called.
     *
     * @return A {@link StateInfo} object which contains information about the available states. If no such information
     *   exists {@code null} is returned.
     */
    public StateInfo getStateInfo() {
	throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Get the value of the property Protocol.
     *
     * @return A string containing the protocol uri of this marker type.
     */
    public String getProtocol() {
	return protocol;
    }

}
