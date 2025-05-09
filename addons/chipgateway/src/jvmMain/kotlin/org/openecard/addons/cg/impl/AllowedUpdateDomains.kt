/****************************************************************************
 * Copyright (C) 2016-2025 ecsec GmbH.
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
package org.openecard.addons.cg.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.common.util.DomainUtils
import org.openecard.common.util.FileUtils
import org.openecard.common.util.FileUtils.resolveResourceAsStream
import java.io.IOException

private val logger = KotlinLogging.logger { }

/**
 *
 * @author Tobias Wich
 */
class AllowedUpdateDomains private constructor() {
	private val domainNames = readFile("chipgateway/allowed_update_domains")

	fun isAllowedDomain(domainName: String): Boolean {
		domainNames
			.firstOrNull { DomainUtils.checkWildcardHostName(it, domainName) }
			?.let { return true }
		return false
	}

	private fun readFile(fname: String): List<String> {
		try {
			resolveResourceAsStream(javaClass, fname).use { `in` ->
				return FileUtils.readLinesFromConfig(`in`!!)
			}
		} catch (ex: IOException) {
			logger.error(ex) { "Failed to read allowed update domains file." }
		}

		return emptyList()
	}

	companion object {
		private var inst: AllowedUpdateDomains? = null

		fun instance(): AllowedUpdateDomains {
			// synchronization not needed, because at worst we load it several times
			if (inst == null) {
				inst = AllowedUpdateDomains()
			}
			return inst!!
		}
	}
}
