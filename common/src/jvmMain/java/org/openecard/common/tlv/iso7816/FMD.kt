/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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
import org.openecard.common.util.Pair
import java.util.LinkedList

/**
 * The class model an FMD as data type.
 *
 * @author Hans-Martin Haase
 */
class FMD(
	private val tlv: TLV,
) {
	var content: Boolean = true

	/**
	 * Get the discretionary data if available.
	 *
	 * @return The Discretionary Data contained in the FMD.
	 */
	var discretionaryData: ByteArray? = null
		private set

	/**
	 * Get the discretionary data template which may contain proprietary information.
	 *
	 * @return A discretionary data template as byte array.
	 */
	var discretionaryDataTemplate: ByteArray? = null
		private set
	private var applicationTemplates: MutableList<ApplicationTemplate>? = null

	/**
	 * Get the name of the application.
	 *
	 * @return A string containing the name of the application.
	 */
	var applicationLabel: String? = null
		private set

	/**
	 * Get the file reference of the application or file.
	 *
	 * @return A file reference to the current application.
	 */
	var fileReference: ByteArray? = null
		private set

	/**
	 * Get the uniform resource locator which points to the part of the software required in the interface device to
	 * communicate with the application in the card.
	 *
	 * @return The URL contained in the FMD.
	 */
	var uRL: String? = null
		private set
	private var references: MutableList<Pair<ByteArray?, ByteArray?>>? = null
	private var proprietaryInformation: MutableList<TLV?>? = null

	/**
	 * Creats an FMD object.
	 *
	 * @param tlv
	 * @throws TLVException
	 * @throws UnsupportedEncodingException
	 */
	init {
		if (tlv.tagNumWithClass != 0x64L) {
			throw TLVException("Data doesn't represent an FCP.")
		}

		if (tlv.value!!.isEmpty()) {
			content = false
		} else {
			val child = tlv.child

			if (child!!.tagNumWithClass == 0x61L) {
				val p = Parser(child)
				applicationTemplates = LinkedList()
				while (p.match(0x61)) {
					if (p.match(0x61)) {
						(applicationTemplates as LinkedList<ApplicationTemplate>).add(ApplicationTemplate(p.next(0)!!))
					}
				}
			} else if (child.tagNumWithClass == 0x53L) {
				discretionaryData = child.value
			} else if (child.tagNumWithClass == 0x73L) {
				discretionaryDataTemplate = child.value
			} else if (child.tagNumWithClass == 0x5F50L) {
				uRL = String(child.value!!, charset("ASCII-US"))
			} else if (child.tagNumWithClass == 0x50L) {
				applicationLabel = String(child.value!!)
			} else if (child.tagNumWithClass == 0x51L) {
				fileReference = child.value
			} else if (child.tagNumWithClass == 0xA2L) {
				val p = Parser(child)
				references = ArrayList()
				while (p.match(0x88) || p.match(0x51)) {
					var shortRef: ByteArray? = null
					var fileRef: ByteArray? = null

					if (p.match(0x88)) {
						shortRef = p.next(0)!!.value
					}

					if (p.match(0x51)) {
						fileRef = p.next(0)!!.value
					}

					val refPair = Pair(shortRef, fileRef)
					(references as ArrayList<Pair<ByteArray?, ByteArray?>>).add(refPair)
				}
			} else if (child.tagNumWithClass == 0x85L) {
				val p = Parser(child)
				proprietaryInformation = ArrayList()
				while (p.match(0x85)) {
					(proprietaryInformation as ArrayList<TLV?>).add(p.next(0))
				}
			}
		}
	}

	/**
	 * Get a list of pairs with short file reference and file reference to files contained in the current application.
	 *
	 * @return A list of pairs where the first component of the pair is the short reference and the second one the full
	 * file reference.
	 */
	fun getReferences(): List<Pair<ByteArray?, ByteArray?>>? = references

	/**
	 * Get a list with proprietary information objects contained in the FMD if available.
	 *
	 * @return List of TLV object which contain the proprietary information.
	 */
	fun getProprietaryInformation(): List<TLV?>? = proprietaryInformation

	/**
	 * The method indicates whether the FMD has content or not.
	 *
	 * @return True if the FMD contains content else false.
	 */
	fun hasContent(): Boolean = content

	/**
	 * Get, if available, the application templates in the FMD.
	 *
	 * @return A list of ApplicationTemplates.
	 */
	fun getApplicationTemplates(): List<ApplicationTemplate>? = applicationTemplates
}
