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
import org.openecard.bouncycastle.asn1.x500.X500Name
import org.openecard.common.util.FileUtils
import org.openecard.common.util.FileUtils.resolveResourceAsStream
import java.io.IOException
import javax.security.auth.x500.X500Principal

private val LOG = KotlinLogging.logger { }

/**
 *
 * @author Tobias Wich
 */
abstract class X500NameStore protected constructor(
	fileName: String?,
) {
	private val subjectNames = convertX500Names(readFile(fileName))

	private fun readFile(fname: String?): List<String> =
		try {
			if (fname == null) {
				listOf()
			} else {
				resolveResourceAsStream(javaClass, fname).use { `in` ->
					FileUtils.readLinesFromConfig(`in`!!)
				}
			}
		} catch (ex: IOException) {
			LOG.error(ex) { "Failed to read allowed subjects file." }
			listOf()
		}

	fun isInSubjects(subject: String): Boolean = subjectNames.contains(X500Name(subject))

	fun isInSubjects(subj: X500Principal): Boolean = isInSubjects(subj.name)

	private fun convertX500Names(subjectNameStrs: Collection<String>) =
		subjectNameStrs.map {
			X500Name(it)
		}
}
