/****************************************************************************
 * Copyright (C) 2012-2017 HS Coburg.
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
package org.openecard.crypto.common.asn1.cvc

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.common.tlv.TLV
import org.openecard.common.tlv.TLVException
import org.openecard.common.tlv.TagClass
import org.openecard.common.util.ByteUtils
import org.openecard.crypto.common.asn1.eac.oid.CVCertificatesObjectIdentifier
import org.openecard.crypto.common.asn1.utils.ObjectIdentifierUtils
import org.openecard.i18n.I18N
import java.util.EnumMap
import java.util.TreeMap

private val LOG = KotlinLogging.logger {}

/**
 * Implements the Certificate Holder Authorization Template (CHAT)
 *
 * See BSI-TR-03110, version 2.10, part 3, section C.4.
 *
 * @author Moritz Horsch
 * @author Dirk Petrautzki
 * @author Tobias Wich
 */
class CHAT {
	private val oid: String

	/**
	 * Returns the role of the CHAT.
	 *
	 * @return Write access
	 */
	val role: Role?
	private val discretionaryData: ByteArray?
	private val writeAccess: EnumMap<DataGroup, Boolean>
	private val readAccess: EnumMap<DataGroup, Boolean>
	private val specialFunctions: EnumMap<SpecialFunction, Boolean>
	private val accessRights: EnumMap<AccessRight, Boolean>

	/**
	 * Represents the roles.
	 * See BSI-TR-03110, version 2.10, part 3, section C.4.1.
	 * See BSI-TR-03110, version 2.10, part 3, section C.4.2.
	 * See BSI-TR-03110, version 2.10, part 3, section C.4.3.
	 */
	enum class Role {
		CVCA,
		DV_OFFICIAL,
		DV_NON_OFFICIAL,
		AUTHENTICATION_TERMINAL,
		INSPECTION_TERMINAL,
		SIGNATURE_TERMINAL,
	}

	/**
	 * Represents the special functions.
	 * See BSI-TR-03110, version 2.10, part 3, section C.4.2.
	 */
	enum class SpecialFunction {
		INSTALL_QUALIFIED_CERTIFICATE,
		INSTALL_CERTIFICATE,
		PIN_MANAGEMENT,
		CAN_ALLOWED,
		PRIVILEGED_TERMINAL,
		RESTRICTED_IDENTIFICATION,
		COMMUNITY_ID_VERIFICATION,
		AGE_VERIFICATION,
	}

	/**
	 * Represents the specialData groups.
	 * See BSI-TR-03110, version 2.10, part 3, section C.4.2.
	 * See BSI-TR-03110, version 2.10, part 2, section A.1.
	 */
	enum class DataGroup {
		DG01,
		DG02,
		DG03,
		DG04,
		DG05,
		DG06,
		DG07,
		DG08,
		DG09,
		DG10,
		DG11,
		DG12,
		DG13,
		DG14,
		DG15,
		DG16,
		DG17,
		DG18,
		DG19,
		DG20,
		DG21,
		DG22,
	}

	/**
	 * Represents the access rights.
	 * See BSI-TR-03110, version 2.10, part 3, section C.4.1.
	 * See BSI-TR-03110, version 2.10, part 3, section C.4.3.
	 */
	enum class AccessRight {
		DG03,
		DG04,
		GENERATE_SIGNATURE,
		GENERATE_QUALIFIED_SIGNATURE,
	}

	constructor() {
		oid = CVCertificatesObjectIdentifier.id_AT
		role = Role.AUTHENTICATION_TERMINAL
		discretionaryData = null

		writeAccess = EnumMap(DataGroup::class.java)
		readAccess = EnumMap(DataGroup::class.java)
		specialFunctions = EnumMap(SpecialFunction::class.java)
		accessRights = EnumMap(AccessRight::class.java)
		initMaps()
	}

	/**
	 * Creates a new CHAT.
	 *
	 * @param chat CHAT
	 * @throws TLVException
	 */
	constructor(chat: ByteArray) : this(TLV.fromBER(chat))

	/**
	 * Creates a new CHAT.
	 *
	 * @param tlv TLV
	 * @throws TLVException
	 */
	constructor(tlv: TLV) {
		oid = ObjectIdentifierUtils.toString(tlv.findChildTags(0x06)[0].value)
		discretionaryData = tlv.findChildTags(0x53)[0].value

		writeAccess = EnumMap(DataGroup::class.java)
		readAccess = EnumMap(DataGroup::class.java)
		specialFunctions = EnumMap(SpecialFunction::class.java)
		accessRights = EnumMap(AccessRight::class.java)
		initMaps()

		when (oid) {
			CVCertificatesObjectIdentifier.id_IS -> {
				// Inspection systems
				role = parseRole(discretionaryData[0])
				parseAccessRights(discretionaryData[0])
			}

			CVCertificatesObjectIdentifier.id_AT -> {
				// Authentication terminal
				role = parseRole(discretionaryData[0])
				parseWriteAccess(discretionaryData)
				parseReadAccess(discretionaryData)
				parseSpecialFunctions(discretionaryData)
			}

			CVCertificatesObjectIdentifier.id_ST -> {
				// Signature terminal
				role = parseRole(discretionaryData[0])
				parseAccessRights(discretionaryData[0])
			}

			else -> role = null
		}
	}

	private fun initMaps() {
		// eID rights
		val dataGroups: Array<DataGroup?> = DataGroup.entries.toTypedArray()
		for (i in 16..21) {
			writeAccess.put(dataGroups[i], false)
		}
		for (i in 0..21) {
			readAccess.put(dataGroups[i], false)
		}
		// Special eID functions
		for (data in SpecialFunction.entries) {
			specialFunctions.put(data, false)
		}
		// Inspection Systems (passport)/ Signature Terminal rights
		for (data in AccessRight.entries) {
			accessRights.put(data, false)
		}
	}

	/**
	 * Parse the role of the CHAT.
	 *
	 * @param roleByte Role
	 */
	private fun parseRole(roleByte: Byte): Role? {
		var roleByte = roleByte
		roleByte = (roleByte.toInt() and 0xC0.toByte().toInt()).toByte()

		when (roleByte) {
			0xC0.toByte() -> return Role.CVCA
			0x80.toByte() -> return Role.DV_OFFICIAL
			0x40.toByte() -> return Role.DV_NON_OFFICIAL
			0x00.toByte() -> {
				when (oid) {
					CVCertificatesObjectIdentifier.id_IS -> return Role.INSPECTION_TERMINAL
					CVCertificatesObjectIdentifier.id_AT -> return Role.AUTHENTICATION_TERMINAL
					CVCertificatesObjectIdentifier.id_ST -> return Role.SIGNATURE_TERMINAL
				}
				return null
			}

			else -> return null
		}
	}

	/**
	 * Parse the access rights of the CHAT.
	 *
	 * @param accessRightsByte Access rights
	 */
	private fun parseAccessRights(accessRightsByte: Byte) {
		val data = byteArrayOf(accessRightsByte)
		if (role == Role.INSPECTION_TERMINAL) {
			// Read access to ePassport application: DG 4 (Iris)
			accessRights.put(AccessRight.DG04, ByteUtils.isBitSet(6, data))
			// Read access to ePassport application: DG 3 (Fingerprint)
			accessRights.put(AccessRight.DG03, ByteUtils.isBitSet(7, data))
		} else if (role == Role.SIGNATURE_TERMINAL) {
			// Generate qualified electronic signature
			accessRights.put(AccessRight.GENERATE_QUALIFIED_SIGNATURE, ByteUtils.isBitSet(6, data))
			// Generate electronic signature
			accessRights.put(AccessRight.GENERATE_SIGNATURE, ByteUtils.isBitSet(7, data))
		}
	}

	/**
	 * Parse the write access of the CHAT.
	 *
	 * @param discretionaryData Discretionary specialData
	 */
	private fun parseWriteAccess(discretionaryData: ByteArray) {
		val it = writeAccess.keys.iterator()
		for (i in 2..7) {
			val item = it.next()
			writeAccess.put(item, ByteUtils.isBitSet(i, discretionaryData))
		}
	}

	/**
	 * Parse the read access of the CHAT.
	 *
	 * @param discretionaryData Discretionary specialData
	 */
	private fun parseReadAccess(discretionaryData: ByteArray) {
		val it = readAccess.keys.iterator()
		for (i in 31 downTo 11) {
			val item = it.next()
			readAccess.put(item, ByteUtils.isBitSet(i, discretionaryData))
		}
	}

	/**
	 * Parse the special functions of the CHAT.
	 *
	 * @param discretionaryData Discretionary specialData
	 */
	private fun parseSpecialFunctions(discretionaryData: ByteArray) {
		val it = specialFunctions.keys.iterator()
		for (i in 32..39) {
			val item = it.next()
			specialFunctions.put(item, ByteUtils.isBitSet(i, discretionaryData))
		}
	}

	/**
	 * Returns the write access of the CHAT.
	 * The returned map is immutable.
	 *
	 * @return Write access map containing all write access values.
	 */
	fun getWriteAccess(): Map<DataGroup, Boolean> = writeAccess.toMap()

	/**
	 * Sets the write access of the CHAT.
	 *
	 * @param dataGroup Data group
	 * @param selected Selected
	 * @return True if specialData group is set, otherwise false
	 */
	fun setWriteAccess(
		dataGroup: DataGroup,
		selected: Boolean,
	): Boolean {
		if (writeAccess.containsKey(dataGroup)) {
			writeAccess.put(dataGroup, selected)
			return true
		}
		return false
	}

	/**
	 * Sets the write access of the CHAT.
	 *
	 * @param dataGroup Data Group
	 * @param selected Selected
	 * @return True if specialData group is set, otherwise false
	 */
	fun setWriteAccess(
		dataGroup: String,
		selected: Boolean,
	): Boolean = setWriteAccess(DataGroup.valueOf(dataGroup), selected)

	/**
	 * Sets the write access of the CHAT.
	 *
	 * @param writeAccess Write access
	 */
	fun setWriteAccess(writeAccess: TreeMap<DataGroup, Boolean>) {
		val it = this.writeAccess.keys.iterator()
		while (it.hasNext()) {
			val item = it.next()
			if (this.writeAccess.containsKey(item)) {
				this.writeAccess.put(item, writeAccess.get(item))
			}
		}
	}

	/**
	 * Returns the read access of the CHAT.
	 * The returned map is immutable.
	 *
	 * @return Read access map containing all read access values.
	 */
	fun getReadAccess(): Map<DataGroup, Boolean> = readAccess.toMap()

	/**
	 * Sets the read access of the CHAT.
	 *
	 * @param dataGroup Data group
	 * @param selected Selected
	 * @return True if the specialData group is set, otherwise false
	 */
	fun setReadAccess(
		dataGroup: DataGroup,
		selected: Boolean,
	): Boolean {
		if (readAccess.containsKey(dataGroup)) {
			readAccess.put(dataGroup, selected)
			return true
		}
		return false
	}

	/**
	 * Sets the read access of the CHAT.
	 *
	 * @param dataGroup Data group
	 * @param selected Selected
	 * @return True if the specialData group is set, otherwise false
	 */
	fun setReadAccess(
		dataGroup: String,
		selected: Boolean,
	): Boolean = setReadAccess(DataGroup.valueOf(dataGroup), selected)

	/**
	 * Sets the read access of the CHAT.
	 *
	 * @param readAccess Read access
	 */
	fun setReadAccess(readAccess: TreeMap<DataGroup, Boolean>) {
		val it = this.readAccess.keys.iterator()
		while (it.hasNext()) {
			val item = it.next()
			if (this.readAccess.containsKey(item)) {
				this.readAccess.put(item, readAccess.get(item))
			}
		}
	}

	/**
	 * Returns the special function of the CHAT.
	 * The returned map is immutable.
	 *
	 * @return Special functions map containing all special functions values.
	 */
	fun getSpecialFunctions(): Map<SpecialFunction, Boolean> = specialFunctions.toMap()

	/**
	 * Sets the special functions of the CHAT.
	 *
	 * @param specialFunction Special functions
	 * @param selected Selected
	 * @return True if the special function is set, otherwise false
	 */
	fun setSpecialFunctions(
		specialFunction: SpecialFunction,
		selected: Boolean,
	): Boolean {
		if (specialFunctions.containsKey(specialFunction)) {
			specialFunctions.put(specialFunction, selected)
			return true
		}
		return false
	}

	/**
	 * Sets the given special function of the CHAT.
	 *
	 * @param specialFunction Special function
	 * @param selected Selected
	 * @return True if the special function is set, otherwise false
	 */
	fun setSpecialFunction(
		specialFunction: String,
		selected: Boolean,
	): Boolean = setSpecialFunctions(SpecialFunction.valueOf(specialFunction), selected)

	/**
	 * Sets the special functions of the CHAT.
	 *
	 * @param specialFunctions Special functions
	 */
	fun setSpecialFunctions(specialFunctions: TreeMap<SpecialFunction, Boolean>) {
		val it = this.specialFunctions.keys.iterator()
		while (it.hasNext()) {
			val item = it.next()
			if (this.specialFunctions.containsKey(item)) {
				this.specialFunctions.put(item, specialFunctions.get(item))
			}
		}
	}

	/**
	 * Returns the access rights of the CHAT.
	 * The returned map is immutable.
	 *
	 * @return Access rights map containg all access rights values.
	 */
	fun getAccessRights(): Map<AccessRight, Boolean> = accessRights.toMap()

	/**
	 * Sets the special functions of the CHAT.
	 *
	 * @param accessRight Access right
	 * @param selected Selected
	 * @return True if the access right is set, otherwise false
	 */
	fun setAccessRights(
		accessRight: AccessRight,
		selected: Boolean,
	): Boolean {
		if (accessRights.containsKey(accessRight)) {
			accessRights.put(accessRight, selected)
			return true
		}
		return false
	}

	/**
	 * Sets the special functions of the CHAT.
	 *
	 * @param accessRight Access right
	 * @param selected Selected
	 * @return True if the access right is set, otherwise false
	 */
	fun setAccessRights(
		accessRight: String,
		selected: Boolean,
	): Boolean = setAccessRights(AccessRight.valueOf(accessRight), selected)

	/**
	 * Sets the special functions of the CHAT.
	 *
	 * @param accessRights Access right
	 */
	fun setAccessRights(accessRights: Map<AccessRight, Boolean>) {
		val it = this.accessRights.keys.iterator()
		while (it.hasNext()) {
			val item = it.next()
			if (this.accessRights.containsKey(item)) {
				this.accessRights.put(item, accessRights[item])
			}
		}
	}

	/**
	 * Restricts this CHAT by using the given CHAT as a mask.
	 *
	 * @param mask CHAT to use as mask.
	 */
	fun restrictAccessRights(mask: CHAT) {
		removeRights(readAccess, mask.readAccess)
		removeRights(writeAccess, mask.writeAccess)
		removeRights(specialFunctions, mask.specialFunctions)
		removeRights(accessRights, mask.accessRights)
	}

	/**
	 * Returns the CHAT as a byte array.
	 *
	 * @return CHAT
	 * @throws TLVException
	 */
	@Throws(TLVException::class)
	fun toByteArray(): ByteArray {
		val data = ByteArray(5)

		// Decode role in bit 0 to 1.
		when (role) {
			Role.CVCA -> data[0] = (data[0].toInt() or 0xC0).toByte()
			Role.DV_OFFICIAL -> data[0] = (data[0].toInt() or 0x80).toByte()
			Role.DV_NON_OFFICIAL -> data[0] = (data[0].toInt() or 0x40).toByte()
			else -> {}
		}

		// Decode write access in bit 2 to 8.
		val it1 = writeAccess.keys.iterator()
		for (i in 2..7) {
			val item = it1.next()
			if (writeAccess[item] == true) {
				ByteUtils.setBit(i, data)
			}
		}

		// Decode read access in bit 10 to 31.
		val it2 = readAccess.keys.iterator()
		for (i in 31 downTo 11) {
			val item = it2.next()
			if (readAccess[item] == true) {
				ByteUtils.setBit(i, data)
			}
		}

		// Decode special functions in bit 32 to 40.
		val it3 = specialFunctions.keys.iterator()
		for (i in 32..39) {
			val item = it3.next()
			if (specialFunctions[item] == true) {
				ByteUtils.setBit(i, data)
			}
		}

		// Decode access rights in bit 2 to 7.
		val it4 = accessRights.keys.iterator()
		for (i in 6..6) {
			val item = it4.next()
			if (accessRights[item] == true) {
				ByteUtils.setBit(i, data)
			}
		}

		val discretionaryDataObject = TLV()
		discretionaryDataObject.setTagNumWithClass(0x53.toByte())
		discretionaryDataObject.value = data

		val oidObject = TLV()
		oidObject.setTagNumWithClass(0x06.toByte())
		oidObject.value = ObjectIdentifierUtils.getValue(oid)
		oidObject.addToEnd(discretionaryDataObject)

		val chatObject = TLV()
		chatObject.setTagNum(0x4C.toByte())
		chatObject.tagClass = TagClass.APPLICATION
		chatObject.child = oidObject

		return chatObject.toBER(true)
	}

	/**
	 * Compares the CHAT.
	 *
	 * @param chat CHAT
	 * @return True if the CHATs are equal, otherwise false
	 */
	fun compareTo(chat: CHAT): Boolean =
		try {
			ByteUtils.compare(toByteArray(), chat.toByteArray())
		} catch (e: Exception) {
			false
		}

	/**
	 * Returns the CHAT as a hex encoded string.
	 *
	 * @return CHAT
	 */
	fun toHexString(): String? {
		try {
			return ByteUtils.toHexString(toByteArray(), true)
		} catch (ex: TLVException) {
			LOG.error(ex) { "${ex.message}" }
			return ""
		}
	}

	override fun toString(): String {
		try {
			return ByteUtils.toHexString(toByteArray())!!
		} catch (ex: TLVException) {
			LOG.error(ex) { "${ex.message}" }
			return ""
		}
	}

	companion object {
		private fun <T : Enum<T>> removeRights(
			orig: EnumMap<T, Boolean>,
			mask: EnumMap<T, Boolean>,
		) {
			for (entry in mask.entries) {
				if (entry.value == false) {
					orig.put(entry.key, false)
				}
			}
		}
	}
}

fun CHAT.DataGroup.stringResource() =
	when (this) {
		CHAT.DataGroup.DG01 -> I18N.strings.eac_dg01
		CHAT.DataGroup.DG02 -> I18N.strings.eac_dg02
		CHAT.DataGroup.DG03 -> I18N.strings.eac_dg03
		CHAT.DataGroup.DG04 -> I18N.strings.eac_dg04
		CHAT.DataGroup.DG05 -> I18N.strings.eac_dg05
		CHAT.DataGroup.DG06 -> I18N.strings.eac_dg06
		CHAT.DataGroup.DG07 -> I18N.strings.eac_dg07
		CHAT.DataGroup.DG08 -> I18N.strings.eac_dg08
		CHAT.DataGroup.DG09 -> I18N.strings.eac_dg09
		CHAT.DataGroup.DG10 -> I18N.strings.eac_dg10
		CHAT.DataGroup.DG11 -> I18N.strings.eac_dg11
		CHAT.DataGroup.DG12 -> I18N.strings.eac_dg12
		CHAT.DataGroup.DG13 -> I18N.strings.eac_dg13
		CHAT.DataGroup.DG14 -> I18N.strings.eac_dg14
		CHAT.DataGroup.DG15 -> I18N.strings.eac_dg15
		CHAT.DataGroup.DG16 -> I18N.strings.eac_dg16
		CHAT.DataGroup.DG17 -> I18N.strings.eac_dg17
		CHAT.DataGroup.DG18 -> I18N.strings.eac_dg18
		CHAT.DataGroup.DG19 -> I18N.strings.eac_dg19
		CHAT.DataGroup.DG20 -> I18N.strings.eac_dg20
		CHAT.DataGroup.DG21 -> I18N.strings.eac_dg21
		CHAT.DataGroup.DG22 -> I18N.strings.eac_dg22
	}

fun CHAT.SpecialFunction.stringResource() =
	when (this) {
		CHAT.SpecialFunction.INSTALL_QUALIFIED_CERTIFICATE -> I18N.strings.eac_install_qualified_certificate
		CHAT.SpecialFunction.INSTALL_CERTIFICATE -> I18N.strings.eac_install_certificate
		CHAT.SpecialFunction.PIN_MANAGEMENT -> I18N.strings.eac_pin_management
		CHAT.SpecialFunction.CAN_ALLOWED -> I18N.strings.eac_can_allowed
		CHAT.SpecialFunction.PRIVILEGED_TERMINAL -> I18N.strings.eac_privileged_terminal
		CHAT.SpecialFunction.RESTRICTED_IDENTIFICATION -> I18N.strings.eac_restricted_identification
		CHAT.SpecialFunction.COMMUNITY_ID_VERIFICATION -> I18N.strings.eac_community_id_verification
		CHAT.SpecialFunction.AGE_VERIFICATION -> I18N.strings.eac_age_verification
	}
