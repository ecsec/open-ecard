/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
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

package org.openecard.common.sal.anytype;

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
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * @author Hans-Martin Haase <hans-martin.haase@ecsec.de>
 */
public class CryptoMarkerType {

    private String legacyKeyName = null;
    private AlgorithmInfoType algorithmInfo = null;
    private HashGenerationInfoType hashGenerationInfo = null;
    private CertificateRefType certificateRef = null;
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
	    if (elem.getLocalName().equals("AlgorithmInfo")) {
		algorithmInfo = new AlgorithmInfoType();
		NodeList algorithmInfoNodes = elem.getChildNodes();
		for (int i = 0; i < algorithmInfoNodes.getLength(); i++) {
		    Node node = algorithmInfoNodes.item(i);
		    if (node.getLocalName().equals("Algorithm")) {
			algorithmInfo.setAlgorithm(node.getTextContent());
		    } else if (node.getLocalName().equals("AlgorithmIdentifier")) {
			AlgorithmIdentifierType algorithmIdentifierType = new AlgorithmIdentifierType();
			NodeList nodeList = node.getChildNodes();
			for (int y = 0; y < nodeList.getLength(); y++) {
			    Node n = nodeList.item(y);
			    if (n.getLocalName().equals("Algorithm")) {
				algorithmIdentifierType.setAlgorithm(n.getTextContent());
			    } else if (n.getLocalName().equals("Parameters")) {
				algorithmIdentifierType.setParameters(n);
			    }
			}
			algorithmInfo.setAlgorithmIdentifier(algorithmIdentifierType);
		    } else if (node.getLocalName().equals("SupportedOperations")) {
			String[] supportedOperations = node.getTextContent().split(" ");
			algorithmInfo.getSupportedOperations().addAll(Arrays.asList(supportedOperations));
		    } else if (node.getLocalName().equals("CardAlgRef")) {
			algorithmInfo.setCardAlgRef(StringUtils.toByteArray(node.getTextContent()));
		    } else if (node.getLocalName().equals("HashAlgRef")) {
			algorithmInfo.setHashAlgRef(StringUtils.toByteArray(node.getTextContent()));
		    }
		}
	    } else if (elem.getLocalName().equals("KeyInfo")) {
		cryptoKeyInfo = new CryptoKeyInfoType();
		NodeList nodeList = elem.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
		    Node n = nodeList.item(i);
		    if (n.getLocalName().equals("KeyRef")) {
			KeyRefType keyRef = new KeyRefType();
			keyRef.setKeyRef(StringUtils.toByteArray(n.getTextContent()));
			cryptoKeyInfo.setKeyRef(keyRef);
		    } else if (n.getLocalName().equals("KeySize")) {
			cryptoKeyInfo.setKeySize(new BigInteger(n.getTextContent()));
		    }
		}
	    } else if (elem.getLocalName().equals("SignatureGenerationInfo")) {
		signatureGenerationInfo = elem.getTextContent().split(" ");
	    } else if (elem.getLocalName().equals("LegacySignatureGenerationInfo")) {
		NodeList nodeList = elem.getChildNodes();
		legacySignatureGenerationInfo = new ArrayList<CardCallTemplateType>();
		for (int i = 0; i < nodeList.getLength(); i++) {
		    Node n = nodeList.item(i);
		    if (n.getLocalName().equals("CardCommand")) {
			NodeList nodeList2 = n.getChildNodes();
			CardCallTemplateType cctt = new CardCallTemplateType();
			for (int j = 0; j < nodeList2.getLength(); j++) {
			    Node n2 = nodeList2.item(j);
			    String localName = n2.getLocalName();
			    if (localName.equals("HeaderTemplate")) {
				cctt.setHeaderTemplate(n2.getTextContent());
			    } else if (localName.equals("DataTemplate")) {
				cctt.setDataTemplate(n2.getTextContent());
			    } else if (localName.equals("ExpectedLength")) {
				cctt.setExpectedLength(BigInteger.valueOf(Integer.parseInt(n2.getTextContent())));
			    }
			}
			legacySignatureGenerationInfo.add(cctt);
		    }
		}
	    } else if (elem.getLocalName().equals("HashGenerationInfo")) {
		hashGenerationInfo = HashGenerationInfoType.fromValue(elem.getTextContent());
	    } else if (elem.getLocalName().equals("CertificateRef")) {
		certificateRef = new CertificateRefType();
		NodeList nodeList = elem.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
		    Node n = nodeList.item(i);
		    if (n.getLocalName().equals("DataSetName")) {
			certificateRef.setDataSetName(n.getTextContent());
		    } else if (n.getLocalName().equals("DSIName")) {
			certificateRef.setDSIName(n.getTextContent());
		    } else if (n.getLocalName().equals("CertificateType")) {
			certificateRef.setCertificateType(n.getTextContent());
		    }
		}
	    } else if (elem.getLocalName().equals("LegacyKeyName")) {
		this.legacyKeyName = elem.getTextContent();
	    } else if (elem.getLocalName().equals("StateInfo")) {
		// TODO
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
     * the currently used card. If no such information is available NULL is returned.
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
     * @return A {@link CryptokeyInfoType} object which contains all known information about a key. If no such
     * information is available NULL is returned.
     */
    public CryptoKeyInfoType getCryptoKeyInfo() {
	return cryptoKeyInfo;
    }

    /**
     * Get the value of the property LegacyKeyName if it exists.
     *
     * @return A string containing a key name. If no such key name is available NULL is returned.
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
     * @return A {@link HashGenerationInfoType} object containing all necessary information about the creation of a hash.
     * If no such information is available NULL is returned.
     */
    public HashGenerationInfoType getHashGenerationInfo() {
	return hashGenerationInfo;
    }

    /**
     * Get the value of the property CertificateRef if it exists.
     *
     * @return A {@link CertificateRefType} object which contains a reference to a certificate object. If no such object
     * exists NULL is returned.
     */
    public CertificateRefType getCertificateRef() {
	return certificateRef;
    }

    /**
     * Get the value of the property StateInfo if it exists.
     *
     * @return A {@link StateInfo} object which contains information about the available states. If no such information
     * exists NULL is returned.
     *
     * NOTE: This method is currently not implemented and throws an UnsupportedOperationException if the method is called.
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
