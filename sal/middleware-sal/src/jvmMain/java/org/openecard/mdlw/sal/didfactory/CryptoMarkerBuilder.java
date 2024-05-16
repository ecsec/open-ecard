/****************************************************************************
 * Copyright (C) 2016 ecsec GmbH.
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

package org.openecard.mdlw.sal.didfactory;

import iso.std.iso_iec._24727.tech.schema.AlgorithmInfoType;
import iso.std.iso_iec._24727.tech.schema.CertificateRefType;
import iso.std.iso_iec._24727.tech.schema.CryptoKeyInfoType;
import iso.std.iso_iec._24727.tech.schema.CryptoMarkerType;
import iso.std.iso_iec._24727.tech.schema.HashGenerationInfoType;
import iso.std.iso_iec._24727.tech.schema.LegacySignatureGenerationType;
import java.util.ArrayList;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import org.openecard.ws.marshal.MarshallingTypeException;
import org.openecard.ws.marshal.WSMarshaller;
import org.openecard.ws.marshal.WSMarshallerException;
import org.openecard.ws.marshal.WSMarshallerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;


/**
 *
 * @author Tobias Wich
 */
public class CryptoMarkerBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(CryptoMarkerBuilder.class);
    private static final String PROTOCOL = "urn:oid:1.3.162.15480.3.0.25";
    private static final String ISONS = "urn:iso:std:iso-iec:24727:tech:schema";

    private final WSMarshaller m;

    private AlgorithmInfoType algInfo;
    private CryptoKeyInfoType keyInfo;
    private String sigGenInfo;
    private LegacySignatureGenerationType legacySignGenInfo;
    private HashGenerationInfoType hashGenInfo;
    private ArrayList<CertificateRefType> certRefs;
    private byte[] legacyKeyname;


    public CryptoMarkerBuilder() throws WSMarshallerException {
	m = WSMarshallerFactory.createInstance();
    }

    public void setAlgInfo(AlgorithmInfoType algInfo) {
	this.algInfo = algInfo;
    }

    public void setKeyInfo(CryptoKeyInfoType keyInfo) {
	this.keyInfo = keyInfo;
    }

    public void setSigGenInfo(String sigGenInfo) {
	this.legacySignGenInfo = null;
	this.sigGenInfo = sigGenInfo;
    }

    public void setLegacySignGenInfo(LegacySignatureGenerationType legacySignGenInfo) {
	this.sigGenInfo = null;
	this.legacySignGenInfo = legacySignGenInfo;
    }

    public void setHashGenInfo(HashGenerationInfoType hashGenInfo) {
	this.hashGenInfo = hashGenInfo;
    }

    public ArrayList<CertificateRefType> getCertRefs() {
	if (certRefs == null) {
	    certRefs = new ArrayList<>();
	}
	return certRefs;
    }

    public void setLegacyKeyname(byte[] legacyKeyname) {
	this.legacyKeyname = legacyKeyname;
    }

    public CryptoMarkerType build() {
	CryptoMarkerType marker = new CryptoMarkerType();
	marker.setProtocol(PROTOCOL);

	if (algInfo != null) {
	    try {
		JAXBElement<AlgorithmInfoType> e;
		e = new JAXBElement<>(new QName(ISONS, "AlgorithmInfo"), AlgorithmInfoType.class, algInfo);
		Document d = m.marshal(e);
		marker.getAny().add(d.getDocumentElement());
	    } catch (MarshallingTypeException ex) {
		LOG.error("Failed to marshal AlgorithmInfo element.", ex);
	    }
	}

	if (keyInfo != null) {
	    try {
		JAXBElement<CryptoKeyInfoType> e;
		e = new JAXBElement<>(new QName(ISONS, "KeyInfo"), CryptoKeyInfoType.class, keyInfo);
		Document d = m.marshal(e);
		marker.getAny().add(d.getDocumentElement());
	    } catch (MarshallingTypeException ex) {
		LOG.error("Failed to marshal KeyInfo element.", ex);
	    }
	}

	if (sigGenInfo != null) {
	    try {
		JAXBElement<String> e;
		e = new JAXBElement(new QName(ISONS, "SignatureGenerationInfo"), String.class, sigGenInfo);
		Document d = m.marshal(e);
		marker.getAny().add(d.getDocumentElement());
	    } catch (MarshallingTypeException ex) {
		LOG.error("Failed to marshal SignatureGenerationInfo element.", ex);
	    }
	}

	if (legacySignGenInfo != null) {
	    try {
		JAXBElement<LegacySignatureGenerationType> e;
		e = new JAXBElement(new QName(ISONS, "LegacySignatureGenerationInfo"), LegacySignatureGenerationType.class, legacySignGenInfo);
		Document d = m.marshal(e);
		marker.getAny().add(d.getDocumentElement());
	    } catch (MarshallingTypeException ex) {
		LOG.error("Failed to marshal LegacySignatureGenerationInfo element.", ex);
	    }
	}

	if (hashGenInfo != null) {
	    try {
		JAXBElement<HashGenerationInfoType> e;
		e = new JAXBElement(new QName(ISONS, "HashGenerationInfo"), HashGenerationInfoType.class, hashGenInfo);
		Document d = m.marshal(e);
		marker.getAny().add(d.getDocumentElement());
	    } catch (MarshallingTypeException ex) {
		LOG.error("Failed to marshal HashGenerationInfo element.", ex);
	    }
	}

	for (CertificateRefType certRef : getCertRefs()) {
	    try {
		JAXBElement<CertificateRefType> e;
		e = new JAXBElement(new QName(ISONS, "CertificateRef"), CertificateRefType.class, certRef);
		Document d = m.marshal(e);
		marker.getAny().add(d.getDocumentElement());
	    } catch (MarshallingTypeException ex) {
		LOG.error("Failed to marshal CertificateRef element.", ex);
	    }
	}

	if (legacyKeyname != null) {
	    try {
		JAXBElement<String> e;
		e = new JAXBElement(new QName(ISONS, "LegacyKeyName"), byte[].class, legacyKeyname);
		Document d = m.marshal(e);
		marker.getAny().add(d.getDocumentElement());
	    } catch (MarshallingTypeException ex) {
		LOG.error("Failed to marshal LegacyKeyName element.", ex);
	    }
	}

	return marker;
    }


}
