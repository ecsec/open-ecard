/****************************************************************************
 * Copyright (C) 2012-2014 ecsec GmbH.
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
 * @author Tobias Wich
 */
public class FCP {

    private final TLV tlv;

    private Long numBytes;
    private Long numBytesStructure;
    private DataElements dataElements;
    private final List<byte[]> fileIdentifiers = new LinkedList<>();
    private final List<byte[]> dfNames = new LinkedList<>();
    private final List<byte[]> proprietaryInformationNoTLV = new LinkedList<>();
    private final List<byte[]> proprietarySecurityAttribute = new LinkedList<>();
    private byte[] fciExtensionEf;
    private byte[] shortEfIdentifier;
    private Byte lifeCycleStatusByte;
    private byte[] securityAttributeReferenceExpanded;
    private byte[] securityAttributeCompact;
    private final List<byte[]> securityEnvironmentTemplateEfs = new LinkedList<>();
    private Byte channelSecurityAttribute;
    private byte[] securityAttributeTemplateForDataObject;
    private byte[] securityAttributeTemplateProprietary;
    private final List<TLV> dataObjectTemplates = new LinkedList<>();
    private TLV proprietaryInformationTLV;
    private byte[] securityAttributeTemplateExpanded;
    private byte[] cryptographicMechanismIdentifierTemplate;

    public FCP(TLV tlv) throws TLVException {
	this.tlv = tlv;
	if (tlv.getTagNumWithClass() != 0x62) {
	    throw new TLVException("Data doesn't represent an FCP.");
	}

	// declare helper variables
	List<byte[]> descriptorBytes = new LinkedList<>();

	Parser p = new Parser(tlv.getChild());
	TLV next;
	while ((next = p.next(0)) != null) {
	    // num bytes
	    if (next.getTagNumWithClass() == 0x80) {
		numBytes = ByteUtils.toLong(next.getValue());
	    }
	    if (next.getTagNumWithClass() == 0x81) {
		// length == 2
		numBytesStructure = ByteUtils.toLong(next.getValue());
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
		lifeCycleStatusByte = next.getValue()[0];
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
		channelSecurityAttribute = next.getValue()[0];
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

    /**
     * Gets the corresponding byte array of the FCP object.
     *
     * @return The byte array representation of the FCP.
     */
    public byte[] toBER() {
	return tlv.toBER();
    }


    /**
     * Gets the number of bytes in the select EF.
     * Just present in EFs (once).
     * (Tag 80)
     *
     * @return The number of byte without structural information in the file.
     */
    public Long getNumBytes() {
	return this.numBytes;
    }

    /**
     * Gets the data elements contained in the file descriptor byte.
     * Present in every file.
     * (Tag 82)
     *
     * @return The data elements contained in the file descriptor byte.
     */
    public DataElements getDataElements() {
	return dataElements;
    }

    /**
     * Gets the file identifier of the selected file.
     * len=2
     * Present in every file.
     * (Tag 83)
     *
     * @return A list of byte arrays which contain the file identifiers for the file.
     */
    public List<byte[]> getFileIdentifiers() {
	return fileIdentifiers;
    }

    /**
     * Gets the name of the selected DF as list of byte arrays.
     * len=..*
     * Just present in DFs.
     * (Tag 84)
     *
     * @return A list of byte arrays which contain the names of a DF.
     */
    public List<byte[]> getDfNames() {
	return dfNames;
    }

    /**
     * Gets the number of bytes contained in the file (including structural information).
     * present in any file (once)
     * (Tag 81)
     *
     * @return The number of bytes of the file including structural information.
     */
    public Long getNumBytesStructure() {
	return numBytesStructure;
    }

    /**
     * Gets proprietary information which is not encoded as TLV.
     * present in any file
     * (Tag 85)
     *
     * @return List of byte arrays which contain proprietary information which is not encoded in BER_TLV.
     */
    public List<byte[]> getProprietaryInformationNoTLV() {
	return proprietaryInformationNoTLV;
    }

    /**
     * Gets the proprietary security attributes.
     * present in any file.
     * (Tag 86)
     *
     * @return A list of byte array where the byte array contains a proprietary security attribute.
     */
    public List<byte[]> getProprietarySecurityAttribute() {
	return proprietarySecurityAttribute;
    }

    /**
     * Gets the file id of a EF which contains more information about the file control information.
     * present in DFs (only once)
     * (Tag 87)
     *
     * @return A byte array containing a file identifier.
     */
    public byte[] getFciExtensionEf() {
	return fciExtensionEf;
    }

    /**
     * Gets the short identifier for a EF (just one or zero bytes).
     * present in EFs (only once)
     * (Tag 88)
     *
     * @return The short EF identifier of the file.
     */
    public byte[] getShortEfIdentifier() {
	return shortEfIdentifier;
    }

    /**
     * Get the life cycle status byte of the file.
     * present in every file (only once)
     * (Tag 8A)
     *
     * @return The value of the life cycle status byte property.
     */
    public Byte getLifeCycleStatusByte() {
	return lifeCycleStatusByte;
    }

    /**
     * Gets a security attribute which references the expanded format.
     * present in any file (only once).
     * (Tag 8B)
     *
     * @return A security attribute in expanded format as byte array.
     */
    public byte[] getSecurityAttributeReferenceExpanded() {
	return securityAttributeReferenceExpanded;
    }

    /**
     * Gets a security attribute in compact format.
     * present in any file (only once).
     * (Tag 8C)
     *
     * @return A security attribute in compact format as byte array.
     */
    public byte[] getSecurityAttributeCompact() {
	return securityAttributeCompact;
    }

    /**
     * Gets EF identifier for files which contain SecurityEnvironmentTemplates.
     * present in DFs only
     * (Tag 8D)
     *
     * @return List of byte arrays where the byte arrays contain a file identifier.
     */
    public List<byte[]> getSecurityEnvironmentTemplateEfs() {
	return securityEnvironmentTemplateEfs;
    }

    /**
     * Gets the channel security attribute property.
     * present in any file (only once)
     * (Tag 8E)
     *
     * @return A byte containing the security attributes for the channel.
     */
    public Byte getChannelSecurityAttribute() {
	return channelSecurityAttribute;
    }

    /**
     * Gets the security template for data objects.
     * present in any file (only once)
     * (Tag A0)
     *
     * @return A byte array containing a security attribute template for data objects.
     */
    public byte[] getSecurityAttributeTemplateForDataObject() {
	return securityAttributeTemplateForDataObject;
    }

    /**
     * Gets the security attribute template in a proprietary format.
     * present in any file.
     * (Tag A1)
     *
     * @return Gets the proprietary security attribute as byte array.
     */
    public byte[] getSecurityAttributeTemplateProprietary() {
	return securityAttributeTemplateProprietary;
    }

    /**
     * Gets the data objects which contain file references to EFs.
     * present in DFs only.
     * (Tag A2)
     *
     * @return A list of TLVs which contain the data objects.
     */
    public List<TLV> getDataObjectTemplates() {
	return dataObjectTemplates;
    }

    /**
     * Gets proprietary information in TLV format.
     * present in any file.
     * (Tag A5)
     *
     * @return A TLV containing proprietary information encoded as TLV.
     */
    public TLV getProprietaryInformationTLV() {
	return proprietaryInformationTLV;
    }

    /**
     * Gets the security template in expanded format.
     * present in any file (only once).
     * (Tag AB)
     *
     * @return A byte array containing a security attribute template in expanded format.
     */
    public byte[] getSecurityAttributeTemplateExpanded() {
	return securityAttributeTemplateExpanded;
    }

    /**
     * Get the value of the cryptographic mechanism identifier template.
     * present in DFs only
     * (Tag AC)
     *
     * @return A byte array containing a cryptographic mechanism template.
     */
    public byte[] getCryptographicMechanismIdentifierTemplate() {
	return cryptographicMechanismIdentifierTemplate;
    }

    // TODO: implement further tags in FCP


    /**
     * Create a string from the object which contains all collected information.
     *
     * @param prefix
     * @return A string containing all collected information.
     */
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

    /**
     * Calls the toString(String) method with the empty string as parameter.
     *
     * @return A string containing all collected information.
     */
    @Override
    public String toString() {
	return toString("");
    }

}
