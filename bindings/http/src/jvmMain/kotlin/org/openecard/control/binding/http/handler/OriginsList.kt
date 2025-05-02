/****************************************************************************
 * Copyright (C) 2016-2018 ecsec GmbH.
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
package org.openecard.control.binding.http.handler

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.common.util.FileUtils.homeConfigDir
import org.openecard.common.util.FileUtils.resolveResourceAsStream
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.io.Writer
import java.net.URI
import java.net.URISyntaxException
import java.nio.charset.StandardCharsets

private val logger = KotlinLogging.logger {}

/**
 * Helper for managing a CORS origin whitelist.
 *
 * @author Tobias Wich
 */
object OriginsList {
	private var whitelist: Set<URI>

	init {
		whitelist = load()
	}

	private fun load(): Set<URI> {
		val wl = sortedSetOf<URI>()

		try {
			// read bundled whitelist
			val bundledWl =
				resolveResourceAsStream(
					OriginsList::class.java,
					"/binding/origins.whitelist",
				)
			readWhitelist(wl, bundledWl!!)

			// read user supplied whitelist
			val homePath = homeConfigDir
			val cfgFile = File(homePath, "origins.whitelist")
			if (cfgFile.isFile && cfgFile.canRead()) {
				val homeWl: InputStream = FileInputStream(cfgFile)
				readWhitelist(wl, homeWl)
			}
		} catch (ex: IOException) {
			logger.error(ex) { "Failed to read CORS whitelist." }
		} catch (ex: SecurityException) {
			logger.error(ex) { "Failed to read CORS whitelist." }
		}

		return wl
	}

	private fun readWhitelist(
		wl: MutableSet<URI>,
		`is`: InputStream,
	) {
		val br = BufferedReader(InputStreamReader(`is`, StandardCharsets.UTF_8))
		var nextLine: String
		while ((br.readLine().also { nextLine = it }) != null) {
			nextLine = nextLine.trim { it <= ' ' }
			// skip comments and empty lines
			if (nextLine.startsWith("#") || nextLine.isEmpty()) {
				continue
			}

			// create URI and add it to the list
			try {
				val nextUri = URI(nextLine)
				wl.add(nextUri)
				logger.debug { "Added '$nextLine' to origin whitelist." }
			} catch (ex: URISyntaxException) {
				logger.warn { "Failed to add URL '$nextLine' to the whitelist." }
			}
		}
	}

	val userWhitelist: File
		get() {
			val homePath = homeConfigDir
			val cfgFile = File(homePath, "origins.whitelist")
			if (!cfgFile.exists()) {
				val w: Writer =
					OutputStreamWriter(
						FileOutputStream(cfgFile),
						StandardCharsets.UTF_8,
					)
				PrintWriter(w).use { pw ->
					pw.println("##")
					pw.println("## List of allowed CORS origins")
					pw.println("## ----------------------------")
					pw.println("## Entries must follow the CORS specification. One origin entry per file.")
					pw.println("## Comments begin with the # character.")
					pw.println("## Example: https://example.com")
					pw.println("##")
				}
			}
			return cfgFile
		}

	fun isValidOrigin(origin: String): Boolean {
		val uri = URI(origin)
		return isValidOrigin(uri)
	}

	fun isValidOrigin(origin: URI): Boolean {
		val whitelisted = whitelist.contains(origin)
		return whitelisted
	}
}
