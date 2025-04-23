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
package org.openecard.crypto.common.asn1.eac

/**
 * Implements the SecurityInfos for Chip Authentication.
 * See BSI-TR-03110, version 2.10, part 3, section A.1.1.2.
 *
 * @author Moritz Horsch
 */
class CASecurityInfos {
	/**
	 * Gets the CADomainParameterInfos.
	 */
	var cADomainParameterInfos: MutableList<CADomainParameterInfo> = ArrayList()
	private var cadpiIndex = 0

	/**
	 * Gets the CAInfos.
	 */
	var cAInfos: MutableList<CAInfo> = ArrayList()
	private var caiIndex = 0

	val cADomainParameterInfo: CADomainParameterInfo?
		/**
		 * Returns the selected CADomainParameterInfo.
		 *
		 * @return CADomainParameterInfo
		 */
		get() = cADomainParameterInfos[cadpiIndex]

	/**
	 * Adds a CADomainParameterInfo.
	 *
	 * @param caDomainParameterInfo CADomainParameterInfo
	 */
	fun addCADomainParameterInfo(caDomainParameterInfo: CADomainParameterInfo) {
		this.cADomainParameterInfos.add(caDomainParameterInfo)
	}

	/**
	 * Selects a CADomainParameterInfo.
	 *
	 * @param index Index
	 */
	fun selectCADomainParameterInfo(index: Int) {
		require(!(index < 0 || index > cADomainParameterInfos.size - 1)) { "Index out of range." }
		this.cadpiIndex = index
	}

	val cAInfo: CAInfo?
		/**
		 * Returns the selected CAInfo.
		 *
		 * @return CAInfos
		 */
		get() = cAInfos[caiIndex]

	/**
	 * Adds a CAInfo.
	 *
	 * @param caInfo CAInfo
	 */
	fun addCAInfo(caInfo: CAInfo) {
		this.cAInfos.add(caInfo)
	}

	/**
	 * Selects a CAInfo.
	 *
	 * @param index Index
	 */
	fun selectCAInfo(index: Int) {
		require(!(index < 0 || index > cAInfos.size - 1)) { "Index out of range." }
		this.caiIndex = index
	}

	companion object {
		/**
		 * Checks if the object identifier is a CA object identifier.
		 *
		 * @param oid Object Identifier
		 * @return true if the object identifier is a CA object identifier, otherwise false
		 */
		fun isObjectIdentifier(oid: String): Boolean {
			if (CAInfo.Companion.isObjectIdentifier(oid)) {
				return true
			} else if (CADomainParameterInfo.Companion.isObjectIdentifier(oid)) {
				return true
			}
			return false
		}
	}
}
