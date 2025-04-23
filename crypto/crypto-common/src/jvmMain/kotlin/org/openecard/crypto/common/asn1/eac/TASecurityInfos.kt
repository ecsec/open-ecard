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

import org.openecard.crypto.common.asn1.eac.oid.EACObjectIdentifier

/**
 * Implements the SecurityInfos for Chip Authentication.
 * See BSI-TR-03110, version 2.10, part 3, section A.1.1.3.
 *
 * @author Moritz Horsch
 */
class TASecurityInfos {
	var tAInfos: MutableList<TAInfo> = mutableListOf()
	private var taiIndex = 0

	val tAInfo: TAInfo?
		/**
		 * Returns the selected TAInfo.
		 *
		 * @return TAInfo
		 */
		get() = tAInfos[taiIndex]

	/**
	 * Adds a TAInfo.
	 *
	 * @param taInfo TAInfo
	 */
	fun addTAInfo(taInfo: TAInfo) {
		this.tAInfos.add(taInfo)
	}

	/**
	 * Selects a TAInfo.
	 *
	 * @param index Index
	 */
	fun selectTAInfo(index: Int) {
		require(!(index < 0 || index > tAInfos.size - 1)) { "Index out of range." }
		this.taiIndex = index
	}

	companion object {
		/**
		 * Compares the object identifier.
		 *
		 * @param oid Object identifier
		 * @return true if o is a TA object identifier, else false.
		 */
		fun isObjectIdentifier(oid: String): Boolean = oid == EACObjectIdentifier.ID_TA
	}
}
