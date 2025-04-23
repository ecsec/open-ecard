/****************************************************************************
 * Copyright (C) 2018 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 */
package org.openecard.gui.swing.common

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.common.util.SysUtils
import java.awt.Desktop
import java.io.File
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException

private val LOG = KotlinLogging.logger { }

/**
 *
 * @author Tobias Wich
 */
object SwingUtils {
	/**
	 * Opens a URL in the OS configured default program.
	 * Currently only file and http/https and mailto URLs are tested.
	 * The URL may contain system properties which get expanded when the expand flag is set.
	 *
	 * @param uri URL to open.
	 * @param expandSysProps If `true` expand system properties in the URL, if `false` just use the URL as
	 * it is.
	 */
	@JvmStatic
	fun openUrl(
		uri: URI,
		expandSysProps: Boolean,
	) {
		var uri = uri
		try {
			if (expandSysProps) {
				var urlStr = uri.toString()
				urlStr = SysUtils.expandSysProps(urlStr)
				uri = URI(urlStr)
			}

			var browserOpened = false
			if (Desktop.isDesktopSupported()) {
				if ("file" == uri.scheme && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
					try {
						Desktop.getDesktop().open(File(uri))
						browserOpened = true
					} catch (ex: IOException) {
						// failed to open browser
						LOG.debug(ex) { "${ex.message}" }
					}
				} else if (Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
					try {
						Desktop.getDesktop().browse(uri)
						browserOpened = true
					} catch (ex: IOException) {
						// failed to open browser
						LOG.debug(ex) { "${ex.message}" }
					}
				}
			}
			if (!browserOpened) {
				val openTool: String?
				if (SysUtils.isUnix()) {
					openTool = "xdg-open"
				} else if (SysUtils.isWin()) {
					openTool = "start"
				} else {
					openTool = "open"
				}
				val pb = ProcessBuilder(openTool, uri.toString())
				try {
					pb.start()
				} catch (ex: IOException) {
					// failed to execute command
					LOG.debug(ex) { "${ex.message}" }
				}
			}
		} catch (ex: URISyntaxException) {
			// wrong syntax
			LOG.debug(ex) { "${ex.message}" }
		}
	}
}
