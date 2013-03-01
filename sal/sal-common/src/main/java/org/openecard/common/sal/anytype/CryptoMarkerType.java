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
import iso.std.iso_iec._24727.tech.schema.CertificateRefType;
import iso.std.iso_iec._24727.tech.schema.CryptoKeyInfoType;
import iso.std.iso_iec._24727.tech.schema.HashGenerationInfoType;
import iso.std.iso_iec._24727.tech.schema.KeyRefType;
import iso.std.iso_iec._24727.tech.schema.StateInfo;
import java.math.BigInteger;
import java.util.Arrays;
import org.openecard.common.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class CryptoMarkerType {

    private String legacyKeyName = null;
    private AlgorithmInfoType algorithmInfo = null;
    private HashGenerationInfoType hashGenerationInfo = null;
    private CertificateRefType certificateRef = null;
    private CryptoKeyInfoType cryptoKeyInfo = null;
    private String[] signatureGenerationInfo = null;
    private final String protocol;

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

    public String[] getSignatureGenerationInfo() {
	return signatureGenerationInfo.clone();
    }

    public CryptoKeyInfoType getCryptoKeyInfo() {
	return cryptoKeyInfo;
    }

    public String getLegacyKeyName() {
	return legacyKeyName;
    }

    public AlgorithmInfoType getAlgorithmInfo() {
	return algorithmInfo;
    }

    public HashGenerationInfoType getHashGenerationInfo() {
	return hashGenerationInfo;
    }

    public CertificateRefType getCertificateRef() {
	return certificateRef;
    }

    public StateInfo getStateInfo() {
	throw new UnsupportedOperationException("Not yet implemented");
    }

    public String getProtocol() {
	return protocol;
    }

}
