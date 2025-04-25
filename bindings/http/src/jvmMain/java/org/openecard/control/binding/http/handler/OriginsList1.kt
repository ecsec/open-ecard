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

import org.openecard.common.util.FileUtils.homeConfigDir
import org.openecard.common.util.FileUtils.resolveResourceAsStream
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.*
import java.net.URI
import java.net.URISyntaxException
import java.nio.charset.StandardCharsets
import java.util.*
import javax.annotation.Nonnull

/**
 * Helper for managing a CORS origin whitelist.
 *
 * @author Tobias Wich
 */
object OriginsList {
    private val LOG: Logger = LoggerFactory.getLogger(OriginsList::class.java)

    private var whitelist: Set<URI>? = null

    init {
        load()
    }

    @Synchronized
    private fun load() {
        val wl = TreeSet<URI>()

        try {
            // read bundled whitelist
            val bundledWl = resolveResourceAsStream(
                OriginsList::class.java, "/binding/origins.whitelist"
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
            LOG.error("Failed to read CORS whitelist.", ex)
        } catch (ex: SecurityException) {
            LOG.error("Failed to read CORS whitelist.", ex)
        }

        whitelist = wl
    }

    @Throws(IOException::class)
    private fun readWhitelist(wl: MutableSet<URI>, `is`: InputStream) {
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
                LOG.debug("Added '{}' to origin whitelist.", nextLine)
            } catch (ex: URISyntaxException) {
                LOG.warn("Failed to add URL '{}' to the whitelist.", nextLine)
            }
        }
    }

    @get:Throws(IOException::class, SecurityException::class)
    @get:Nonnull
    val userWhitelist: File
        get() {
            val homePath = homeConfigDir
            val cfgFile = File(homePath, "origins.whitelist")
            if (!cfgFile.exists()) {
                val w: Writer = OutputStreamWriter(
                    FileOutputStream(cfgFile),
                    StandardCharsets.UTF_8
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


    @Throws(URISyntaxException::class)
    fun isValidOrigin(@Nonnull origin: String?): Boolean {
        val uri = URI(origin)
        return isValidOrigin(uri)
    }

    fun isValidOrigin(@Nonnull origin: URI): Boolean {
        val whitelisted = whitelist!!.contains(origin)
        return whitelisted
    }
}
