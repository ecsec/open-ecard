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

import iso.std.iso_iec._24727.tech.schema.KeyRefType;
import iso.std.iso_iec._24727.tech.schema.PasswordAttributesType;
import iso.std.iso_iec._24727.tech.schema.PinCompareMarkerType;
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
public class PinMarkerBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(PinMarkerBuilder.class);
    private static final String PROTOCOL = "urn:oid:1.3.162.15480.3.0.9";
    private static final String ISONS = "urn:iso:std:iso-iec:24727:tech:schema";

    private final WSMarshaller m;

    private KeyRefType pinRef;
    private String pinValue;
    private PasswordAttributesType pwAttributes;

    public PinMarkerBuilder() throws WSMarshallerException {
	m = WSMarshallerFactory.createInstance();
    }

    public void setPinRef(KeyRefType pinRef) {
	this.pinRef = pinRef;
    }

    public void setPinValue(String pinValue) {
	this.pinValue = pinValue;
    }

    public void setPwAttributes(PasswordAttributesType pwAttributes) {
	this.pwAttributes = pwAttributes;
    }

    public PinCompareMarkerType build() {
	PinCompareMarkerType marker = new PinCompareMarkerType();
	marker.setProtocol(PROTOCOL);

	if (pinRef != null) {
	    try {
		JAXBElement<KeyRefType> e;
		e = new JAXBElement<>(new QName(ISONS, "PinRef"), KeyRefType.class, pinRef);
		Document d = m.marshal(e);
		marker.getAny().add(d.getDocumentElement());
	    } catch (MarshallingTypeException ex) {
		LOG.error("Failed to marshal PinRef element.", ex);
	    }
	}

	if (pinValue != null) {
	    try {
		JAXBElement<String> e;
		e = new JAXBElement<>(new QName(ISONS, "PinValue"), String.class, pinValue);
		Document d = m.marshal(e);
		marker.getAny().add(d.getDocumentElement());
	    } catch (MarshallingTypeException ex) {
		LOG.error("Failed to marshal PinValue element.", ex);
	    }
	}

	if (pwAttributes != null) {
	    try {
		JAXBElement<PasswordAttributesType> e;
		e = new JAXBElement(new QName(ISONS, "PasswordAttributes"), PasswordAttributesType.class, pwAttributes);
		Document d = m.marshal(e);
		marker.getAny().add(d.getDocumentElement());
	    } catch (MarshallingTypeException ex) {
		LOG.error("Failed to marshal PasswordAttributes element.", ex);
	    }
	}

	return marker;
    }

}
