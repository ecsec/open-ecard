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
 ***************************************************************************/

package org.openecard.common.tlv.iso7816;

import java.util.LinkedList;
import java.util.List;
import org.openecard.common.tlv.Parser;
import org.openecard.common.tlv.TLV;
import org.openecard.common.tlv.TLVException;
import org.openecard.common.util.ByteUtils;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class FCP {

    private final TLV tlv;

    private Long numBytes;
    private Long numBytesStructure;
    private DataElements dataElements;
    private List<byte[]> fileIdentifiers = new LinkedList<byte[]>();
    private List<byte[]> dfNames = new LinkedList<byte[]>();
    private List<byte[]> proprietaryInformationNoTLV = new LinkedList<byte[]>();
    private List<byte[]> proprietarySecurityAttribute = new LinkedList<byte[]>();
    private byte[] fciExtensionEf;
    private byte[] shortEfIdentifier;
    private Byte lifeCycleStatusByte;
    private byte[] securityAttributeReferenceExpanded;
    private byte[] securityAttributeCompact;
    private List<byte[]> securityEnvironmentTemplateEfs = new LinkedList<byte[]>();
    private Byte channelSecurityAttribute;
    private byte[] securityAttributeTemplateForDataObject;
    private byte[] securityAttributeTemplateProprietary;
    private List<TLV> dataObjectTemplates = new LinkedList<TLV>();
    private TLV proprietaryInformationTLV;
    private byte[] securityAttributeTemplateExpanded;
    private byte[] cryptographicMechanismIdentifierTemplate;

    public FCP(TLV tlv) throws TLVException {
	this.tlv = tlv;
	if (tlv.getTagNumWithClass() != 0x62) {
	    throw new TLVException("Data doesn't represent an FCP.");
	}

	// declare helper variables
	List<byte[]> descriptorBytes = new LinkedList<byte[]>();

	Parser p = new Parser(tlv.getChild());
	TLV next;
	while ((next = p.next(0)) != null) {
	    // num bytes
	    if (next.getTagNumWithClass() == 0x80) {
		numBytes = new Long(ByteUtils.toLong(next.getValue()));
	    }
	    if (next.getTagNumWithClass() == 0x81) {
		// length == 2
		numBytesStructure = new Long(ByteUtils.toLong(next.getValue()));
	    }
	    // descriptor bytes
	    if (next.getTagNumWithClass() == 0x82) {
		descriptorBytes.add(next.getValue());
	    }
	    // file identifier
	    if (next.getTagNumWithClass() == 0x83) {
		fileIdentifiers.add(next.getValue());
	    }
	    // df names
	    if (next.getTagNumWithClass() == 0x84) {
		dfNames.add(next.getValue());
	    }
	    // proprietary information
	    if (next.getTagNumWithClass() == 0x85) {
		proprietaryInformationNoTLV.add(next.getValue());
	    }
	    // proprietary security attribute
	    if (next.getTagNumWithClass() == 0x86) {
		proprietarySecurityAttribute.add(next.getValue());
	    }
	    // file identifier of fci extension
	    if (next.getTagNumWithClass() == 0x87) {
		fciExtensionEf = next.getValue();
	    }
	    // short ef identifier
	    if (next.getTagNumWithClass() == 0x88) {
		shortEfIdentifier = next.getValue();
	    }
	    // lifecycle status byte
	    if (next.getTagNumWithClass() == 0x8A) {
		lifeCycleStatusByte = new Byte(next.getValue()[0]);
	    }
	    // security attribute reference expanded form
	    // TODO: make subtype
	    if (next.getTagNumWithClass() == 0x8B) {
		securityAttributeReferenceExpanded = next.getValue();
	    }
	    // security attribute compact form
	    // TODO: make subtype
	    if (next.getTagNumWithClass() == 0x8C) {
		securityAttributeCompact = next.getValue();
	    }
	    // security environment template EFs
	    if (next.getTagNumWithClass() == 0x8D) {
		securityEnvironmentTemplateEfs.add(next.getValue());
	    }
	    // channel security attribute
	    if (next.getTagNumWithClass() == 0x8E) {
		channelSecurityAttribute = new Byte(next.getValue()[0]);
	    }
	    // securityAttributeTemplateForDataObject
	    if (next.getTagNumWithClass() == 0xA0) {
		securityAttributeTemplateForDataObject = next.getValue();
	    }
	    // securityAttributeTemplateProprietary
	    if (next.getTagNumWithClass() == 0xA1) {
		securityAttributeTemplateProprietary = next.getValue();
	    }
	    // dataObjectTemplates
	    if (next.getTagNumWithClass() == 0xA2) {
		dataObjectTemplates.add(next);
	    }
	    // proprietaryInformationTLV
	    if (next.getTagNumWithClass() == 0xA5) {
		proprietaryInformationTLV = next;
	    }
	    // securityAttributeTemplateExpanded
	    if (next.getTagNumWithClass() == 0xAB) {
		securityAttributeTemplateExpanded = next.getValue();
	    }
	    // cryptographicMechanismIdentifierTemplate
	    if (next.getTagNumWithClass() == 0xAC) {
		cryptographicMechanismIdentifierTemplate = next.getValue();
	    }
	}

	// construct higher level objects
	dataElements = new DataElements(descriptorBytes);
    }

    public FCP(byte[] data) throws TLVException {
	this(TLV.fromBER(data));
    }

    public byte[] toBER() {
	try {
	    return tlv.toBER();
	} catch (TLVException ex) {
	    return null; // can not happen as it is created fom byte[] before
	}
    }


    public Long getNumBytes() {
	return this.numBytes;
    }

    public DataElements getDataElements() {
	return dataElements;
    }

    /** len=2 */
    public List<byte[]> fileIdentifiers() {
	return fileIdentifiers;
    }

    /** len=..* */
    public List<byte[]> DF_Names() {
	return dfNames;
    }



    // TODO: implement further tags in FCP


    public String toString(String prefix) {
	StringBuilder b = new StringBuilder(4096);
	String indent = prefix + " ";
	String subindent = indent + " ";
	b.append(prefix);
	b.append("FCP:\n");
	b.append(indent);
	b.append("num-bytes=");
	b.append(numBytes);
	b.append(" num-bytes-structure=");
	b.append(numBytesStructure);
	b.append("\n");
	b.append(dataElements.toString(indent));
	b.append("\n");
	b.append(indent);
	b.append("File-Identifiers:\n");
	for (byte[] next : fileIdentifiers) {
	    b.append(subindent);
	    b.append(ByteUtils.toHexString(next));
	    b.append("\n");
	}
	b.append("DF-Names:\n");
	for (byte[] next : dfNames) {
	    b.append(subindent);
	    b.append(ByteUtils.toHexString(next));
	    b.append("\n");
	}
	// proprietaryInformationNoTLV
	// proprietarySecurityAttribute
	// fciExtensionEf
	// shortEfIdentifier
	if (shortEfIdentifier != null) {
	    b.append(indent);
	    b.append("short-EF-identifier: ");
	    b.append(ByteUtils.toHexString(shortEfIdentifier));
	    b.append("\n");
	}
	// lifeCycleStatusByte
	// securityAttributeReferenceExpanded
	// securityAttributeCompact
	// securityEnvironmentTemplateEfs
	// channelSecurityAttribute
	// securityAttributeTemplateForDataObject
	// securityAttributeTemplateProprietary
	// dataObjectTemplates
	b.append(indent);
	b.append("DataObjectTemplates:\n");
	for (TLV next : dataObjectTemplates) {
	    b.append(subindent);
	    b.append("DataObjectTemplate:");
	    b.append(next.toString(subindent + " "));
	    b.append("\n");
	}
	// proprietaryInformationTLV
	// securityAttributeTemplateExpanded
	// cryptographicMechanismIdentifierTemplate

	return b.toString();
    }

    @Override
    public String toString() {
	return toString("");
    }

}
