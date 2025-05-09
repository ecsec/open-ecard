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
 */
package org.openecard.common.tlv.iso7816

import org.openecard.common.tlv.Parser
import org.openecard.common.tlv.TLV
import org.openecard.common.tlv.TLVException
import org.openecard.common.util.ByteUtils
import org.openecard.common.util.ByteUtils.toLong
import java.util.LinkedList

/**
 *
 * @author Tobias Wich
 */
class FCP(
	private val tlv: TLV,
) {
	/**
	 * Gets the number of bytes in the select EF.
	 * Just present in EFs (once).
	 * (Tag 80)
	 *
	 * @return The number of byte without structural information in the file.
	 */
	var numBytes: Long? = null
		private set

	/**
	 * Gets the number of bytes contained in the file (including structural information).
	 * present in any file (once)
	 * (Tag 81)
	 *
	 * @return The number of bytes of the file including structural information.
	 */
	var numBytesStructure: Long? = null
		private set

	/**
	 * Gets the data elements contained in the file descriptor byte.
	 * Present in every file.
	 * (Tag 82)
	 *
	 * @return The data elements contained in the file descriptor byte.
	 */
	@JvmField
	val dataElements: DataElements
	private val fileIdentifiers: MutableList<ByteArray?> = LinkedList()
	private val dfNames: MutableList<ByteArray?> = LinkedList()
	private val proprietaryInformationNoTLV: MutableList<ByteArray?> = LinkedList()
	private val proprietarySecurityAttribute: MutableList<ByteArray?> = LinkedList()

	/**
	 * Gets the file id of a EF which contains more information about the file control information.
	 * present in DFs (only once)
	 * (Tag 87)
	 *
	 * @return A byte array containing a file identifier.
	 */
	var fciExtensionEf: ByteArray? = null
		private set

	/**
	 * Gets the short identifier for a EF (just one or zero bytes).
	 * present in EFs (only once)
	 * (Tag 88)
	 *
	 * @return The short EF identifier of the file.
	 */
	var shortEfIdentifier: ByteArray? = null
		private set

	/**
	 * Get the life cycle status byte of the file.
	 * present in every file (only once)
	 * (Tag 8A)
	 *
	 * @return The value of the life cycle status byte property.
	 */
	var lifeCycleStatusByte: Byte? = null
		private set

	/**
	 * Gets a security attribute which references the expanded format.
	 * present in any file (only once).
	 * (Tag 8B)
	 *
	 * @return A security attribute in expanded format as byte array.
	 */
	var securityAttributeReferenceExpanded: ByteArray? = null
		private set

	/**
	 * Gets a security attribute in compact format.
	 * present in any file (only once).
	 * (Tag 8C)
	 *
	 * @return A security attribute in compact format as byte array.
	 */
	var securityAttributeCompact: ByteArray? = null
		private set
	private val securityEnvironmentTemplateEfs: MutableList<ByteArray?> = LinkedList()

	/**
	 * Gets the channel security attribute property.
	 * present in any file (only once)
	 * (Tag 8E)
	 *
	 * @return A byte containing the security attributes for the channel.
	 */
	var channelSecurityAttribute: Byte? = null
		private set

	/**
	 * Gets the security template for data objects.
	 * present in any file (only once)
	 * (Tag A0)
	 *
	 * @return A byte array containing a security attribute template for data objects.
	 */
	var securityAttributeTemplateForDataObject: ByteArray? = null
		private set

	/**
	 * Gets the security attribute template in a proprietary format.
	 * present in any file.
	 * (Tag A1)
	 *
	 * @return Gets the proprietary security attribute as byte array.
	 */
	var securityAttributeTemplateProprietary: ByteArray? = null
		private set
	private val dataObjectTemplates: MutableList<TLV> = LinkedList()

	/**
	 * Gets proprietary information in TLV format.
	 * present in any file.
	 * (Tag A5)
	 *
	 * @return A TLV containing proprietary information encoded as TLV.
	 */
	var proprietaryInformationTLV: TLV? = null
		private set

	/**
	 * Gets the security template in expanded format.
	 * present in any file (only once).
	 * (Tag AB)
	 *
	 * @return A byte array containing a security attribute template in expanded format.
	 */
	var securityAttributeTemplateExpanded: ByteArray? = null
		private set

	/**
	 * Get the value of the cryptographic mechanism identifier template.
	 * present in DFs only
	 * (Tag AC)
	 *
	 * @return A byte array containing a cryptographic mechanism template.
	 */
	var cryptographicMechanismIdentifierTemplate: ByteArray? = null
		private set

	init {
		if (tlv.tagNumWithClass != 0x62L) {
			throw TLVException("Data doesn't represent an FCP.")
		}

		// declare helper variables
		val descriptorBytes: MutableList<ByteArray> = LinkedList()

		val p = Parser(tlv.child)
		var next: TLV? = p.next(0)
		while (next != null) {
			// num bytes
			if (next.tagNumWithClass == 0x80L) {
				numBytes = toLong(next.value!!)
			}
			if (next.tagNumWithClass == 0x81L) {
				// length == 2
				numBytesStructure = toLong(next.value!!)
			}
			// descriptor bytes
			if (next.tagNumWithClass == 0x82L) {
				descriptorBytes.add(next.value!!)
			}
			// file identifier
			if (next.tagNumWithClass == 0x83L) {
				fileIdentifiers.add(next.value)
			}
			// df names
			if (next.tagNumWithClass == 0x84L) {
				dfNames.add(next.value)
			}
			// proprietary information
			if (next.tagNumWithClass == 0x85L) {
				proprietaryInformationNoTLV.add(next.value)
			}
			// proprietary security attribute
			if (next.tagNumWithClass == 0x86L) {
				proprietarySecurityAttribute.add(next.value)
			}
			// file identifier of fci extension
			if (next.tagNumWithClass == 0x87L) {
				fciExtensionEf = next.value
			}
			// short ef identifier
			if (next.tagNumWithClass == 0x88L) {
				shortEfIdentifier = next.value
			}
			// lifecycle status byte
			if (next.tagNumWithClass == 0x8AL) {
				lifeCycleStatusByte = next.value!![0]
			}
			// security attribute reference expanded form
			// TODO: make subtype
			if (next.tagNumWithClass == 0x8BL) {
				securityAttributeReferenceExpanded = next.value
			}
			// security attribute compact form
			// TODO: make subtype
			if (next.tagNumWithClass == 0x8CL) {
				securityAttributeCompact = next.value
			}
			// security environment template EFs
			if (next.tagNumWithClass == 0x8DL) {
				securityEnvironmentTemplateEfs.add(next.value)
			}
			// channel security attribute
			if (next.tagNumWithClass == 0x8EL) {
				channelSecurityAttribute = next.value!![0]
			}
			// securityAttributeTemplateForDataObject
			if (next.tagNumWithClass == 0xA0L) {
				securityAttributeTemplateForDataObject = next.value
			}
			// securityAttributeTemplateProprietary
			if (next.tagNumWithClass == 0xA1L) {
				securityAttributeTemplateProprietary = next.value
			}
			// dataObjectTemplates
			if (next.tagNumWithClass == 0xA2L) {
				dataObjectTemplates.add(next)
			}
			// proprietaryInformationTLV
			if (next.tagNumWithClass == 0xA5L) {
				proprietaryInformationTLV = next
			}
			// securityAttributeTemplateExpanded
			if (next.tagNumWithClass == 0xABL) {
				securityAttributeTemplateExpanded = next.value
			}
			// cryptographicMechanismIdentifierTemplate
			if (next.tagNumWithClass == 0xACL) {
				cryptographicMechanismIdentifierTemplate = next.value
			}
			next = p.next(0)
		}

		// construct higher level objects
		dataElements = DataElements(descriptorBytes)
	}

	@Throws(TLVException::class)
	constructor(data: ByteArray) : this(TLV.Companion.fromBER(data))

	/**
	 * Gets the corresponding byte array of the FCP object.
	 *
	 * @return The byte array representation of the FCP.
	 */
	fun toBER(): ByteArray = tlv.toBER()

	/**
	 * Gets the file identifier of the selected file.
	 * len=2
	 * Present in every file.
	 * (Tag 83)
	 *
	 * @return A list of byte arrays which contain the file identifiers for the file.
	 */
	fun getFileIdentifiers(): List<ByteArray?> = fileIdentifiers

	/**
	 * Gets the name of the selected DF as list of byte arrays.
	 * len=..*
	 * Just present in DFs.
	 * (Tag 84)
	 *
	 * @return A list of byte arrays which contain the names of a DF.
	 */
	fun getDfNames(): List<ByteArray?> = dfNames

	/**
	 * Gets proprietary information which is not encoded as TLV.
	 * present in any file
	 * (Tag 85)
	 *
	 * @return List of byte arrays which contain proprietary information which is not encoded in BER_TLV.
	 */
	fun getProprietaryInformationNoTLV(): List<ByteArray?> = proprietaryInformationNoTLV

	/**
	 * Gets the proprietary security attributes.
	 * present in any file.
	 * (Tag 86)
	 *
	 * @return A list of byte array where the byte array contains a proprietary security attribute.
	 */
	fun getProprietarySecurityAttribute(): List<ByteArray?> = proprietarySecurityAttribute

	/**
	 * Gets EF identifier for files which contain SecurityEnvironmentTemplates.
	 * present in DFs only
	 * (Tag 8D)
	 *
	 * @return List of byte arrays where the byte arrays contain a file identifier.
	 */
	fun getSecurityEnvironmentTemplateEfs(): List<ByteArray?> = securityEnvironmentTemplateEfs

	/**
	 * Gets the data objects which contain file references to EFs.
	 * present in DFs only.
	 * (Tag A2)
	 *
	 * @return A list of TLVs which contain the data objects.
	 */
	fun getDataObjectTemplates(): List<TLV> = dataObjectTemplates

	// TODO: implement further tags in FCP

	/**
	 * Create a string from the object which contains all collected information.
	 *
	 * @param prefix
	 * @return A string containing all collected information.
	 */
	fun toString(prefix: String): String {
		val b = StringBuilder(4096)
		val indent = "$prefix "
		val subindent = "$indent "
		b.append(prefix)
		b.append("FCP:\n")
		b.append(indent)
		b.append("num-bytes=")
		b.append(numBytes)
		b.append(" num-bytes-structure=")
		b.append(numBytesStructure)
		b.append("\n")
		b.append(dataElements.toString(indent))
		b.append("\n")
		b.append(indent)
		b.append("File-Identifiers:\n")
		for (next in fileIdentifiers) {
			b.append(subindent)
			b.append(ByteUtils.toHexString(next))
			b.append("\n")
		}
		b.append("DF-Names:\n")
		for (next in dfNames) {
			b.append(subindent)
			b.append(ByteUtils.toHexString(next))
			b.append("\n")
		}
		// proprietaryInformationNoTLV
		// proprietarySecurityAttribute
		// fciExtensionEf
		// shortEfIdentifier
		if (shortEfIdentifier != null) {
			b.append(indent)
			b.append("short-EF-identifier: ")
			b.append(ByteUtils.toHexString(shortEfIdentifier))
			b.append("\n")
		}
		// lifeCycleStatusByte
		// securityAttributeReferenceExpanded
		// securityAttributeCompact
		// securityEnvironmentTemplateEfs
		// channelSecurityAttribute
		// securityAttributeTemplateForDataObject
		// securityAttributeTemplateProprietary
		// dataObjectTemplates
		b.append(indent)
		b.append("DataObjectTemplates:\n")
		for (next in dataObjectTemplates) {
			b.append(subindent)
			b.append("DataObjectTemplate:")
			b.append(next.toString("$subindent "))
			b.append("\n")
		}

		// proprietaryInformationTLV
		// securityAttributeTemplateExpanded
		// cryptographicMechanismIdentifierTemplate
		return b.toString()
	}

	/**
	 * Calls the toString(String) method with the empty string as parameter.
	 *
	 * @return A string containing all collected information.
	 */
	override fun toString(): String = toString("")
}
