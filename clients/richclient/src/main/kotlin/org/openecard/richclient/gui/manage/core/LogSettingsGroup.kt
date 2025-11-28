/*
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

package org.openecard.richclient.gui.manage.core

import ch.qos.logback.core.joran.spi.JoranException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.common.util.FileUtils.resolveResourceAsStream
import org.openecard.richclient.AddonPropertiesException
import org.openecard.richclient.LogbackConfig
import org.openecard.richclient.gui.manage.SettingsFactory
import org.openecard.richclient.gui.manage.SettingsGroup
import org.openecard.richclient.res.MR
import org.openecard.utils.serialization.XmlUtils
import org.openecard.utils.serialization.XmlUtils.Companion.doc2str
import org.openecard.utils.serialization.XmlUtils.Companion.str2doc
import org.w3c.dom.DOMException
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.xml.sax.SAXException
import java.awt.Desktop
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.net.URISyntaxException
import java.util.Properties
import javax.swing.JComboBox
import javax.swing.event.HyperlinkEvent
import javax.xml.transform.TransformerException
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathExpression
import javax.xml.xpath.XPathExpressionException
import javax.xml.xpath.XPathFactory

private val LOG = KotlinLogging.logger { }

/**
 * Custom settings group for logging settings.
 * The settings are made dynamic to reflect the choice made by the user.
 *
 * @author Tobias Wich
 */
class LogSettingsGroup(
	private val xmlUtils: XmlUtils = XmlUtils(),
) : SettingsGroup(
		MR.strings.addon_core_logging_group_name.localized(),
		SettingsFactory.getInstance(
			loadProperties(xmlUtils),
		),
	) {
	init {
		addSelectionItem(
			MR.strings.addon_core_logging_root.localized(),
			MR.strings.addon_core_logging_root_desc.localized(),
			ROOT_KEY,
			"ERROR",
			"WARN",
			"INFO",
			"DEBUG",
		)
		addLogLevelBox(
			MR.strings.addon_core_logging_eidchannel.localized(),
			MR.strings.addon_core_logging_eidchannel_desc.localized(),
			EID_HTTP_KEY,
		)
		addLogLevelBox(
			MR.strings.addon_core_logging_eac.localized(),
			MR.strings.addon_core_logging_eac_desc.localized(),
			EAC_KEY,
		)
		addLogLevelBox(
			MR.strings.addon_core_logging_pace.localized(),
			MR.strings.addon_core_logging_pace_desc.localized(),
			PACE_KEY,
		)
		addLogLevelBox(
			MR.strings.addon_core_logging_pcsc.localized(),
			MR.strings.addon_core_logging_pcsc_desc.localized(),
			PCSC_KEY,
		)
		addLogLevelBox(
			MR.strings.addon_core_logging_httpbind.localized(),
			MR.strings.addon_core_logging_httpbind_desc.localized(),
			HTTPBIND_KEY,
		)

		// add support text

		// the file debug.html was missing in old i18n
		// deactivate 4 now

		// try {
		// 	val panel: JComponent = createSupportPanel()

		// 	val constraints: GridBagConstraints = GridBagConstraints()
		// 	constraints.insets = Insets(5, 10, 0, 5)
		// 	constraints.fill = GridBagConstraints.NONE
		// 	constraints.gridheight = GridBagConstraints.RELATIVE
		// 	constraints.gridwidth = GridBagConstraints.RELATIVE
		// 	constraints.gridx = 0
		// 	constraints.gridy = itemIdx++
		// 	constraints.anchor = GridBagConstraints.WEST
		// 	container.add(panel, constraints)
		// } catch (ex: IOException) {
		// 	/ / no support panel text available
		// }
	}

	private fun addLogLevelBox(
		name: String,
		desc: String,
		key: String,
	): JComboBox<*> = addSelectionItem(name, desc, key, "", "WARN", "INFO", "DEBUG")

	@Throws(IOException::class, SecurityException::class, AddonPropertiesException::class)
	override fun saveProperties() {
		try {
			val confFile: File = LogbackConfig.confFile

			// create file if needed
			// if (! confFile.exists()) {
			run {
				// overwrite with default settings
				val `is`: InputStream? =
					resolveResourceAsStream(
						LogSettingsGroup::class.java,
						"/logback.xml",
					)
				FileOutputStream(confFile).use { os ->
					val buffer: ByteArray = ByteArray(4096)
					var n: Int
					while ((`is`!!.read(buffer).also { n = it }) > 0) {
						os.write(buffer, 0, n)
					}
				}
			}
			// load file into a Document
			val conf = xmlUtils.w3Builder().str2doc(confFile.inputStream())

			// process root value
			var value: String? = properties.getProperty(ROOT_KEY)
			value = value ?: "ERROR"
			setRootlevel(conf, value)
			// process every value
			value = properties.getProperty(EID_HTTP_KEY)
			value = value ?: ""
			setLoglevel(conf, EID_HTTP_KEY, value)
			value = properties.getProperty(EAC_KEY)
			value = value ?: ""
			setLoglevel(conf, EAC_KEY, value)
			value = properties.getProperty(PACE_KEY)
			value = value ?: ""
			setLoglevel(conf, PACE_KEY, value)
			value = properties.getProperty(PCSC_KEY)
			value = value ?: ""
			setLoglevel(conf, PCSC_KEY, value)
			value = properties.getProperty(HTTPBIND_KEY)
			value = value ?: ""
			setLoglevel(conf, HTTPBIND_KEY, value)
			FileWriter(confFile).use { w ->
				val confStr: String = xmlUtils.transformer().doc2str(conf)
				w.write(confStr)
			}
			// reload log config
			LogbackConfig.load()
		} catch (ex: JoranException) {
			throw AddonPropertiesException(ex.message!!, ex)
		} catch (ex: SAXException) {
			throw IOException(ex.message, ex)
		} catch (ex: TransformerException) {
			throw IOException(ex.message, ex)
		}
	}

	/*
	 * the file debug.html was missing in old i18n
	 * deactivate 4 now
	@Throws(IOException::class)
	private fun createSupportPanel(): JComponent =
		JEditorPane().apply {
			isEditable = false
			contentType = "text/html"
			text = //debug html
			addHyperlinkListener(
				HyperlinkListener { e ->
					openUrl(e)
				},
			)
		}
	 */

	private fun openUrl(event: HyperlinkEvent) {
		val type: HyperlinkEvent.EventType = event.eventType
		if (type == HyperlinkEvent.EventType.ACTIVATED) {
			val url: String = event.url.toExternalForm()
			try {
				var browserOpened: Boolean = false
				val uri = URI(url)
				if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
					try {
						Desktop.getDesktop().browse(uri)
						browserOpened = true
					} catch (ex: IOException) {
						// failed to open browser
						LOG.debug(ex) { "${ex.message}" }
					}
				}
				if (!browserOpened) {
					val pb = ProcessBuilder("xdg-open", uri.toString())
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

	companion object {
		private const val ROOT_KEY: String = "logging.root"
		private const val EID_HTTP_KEY: String = "org.openecard.addons.tr03124.transport"
		private const val EAC_KEY: String = "org.openecard.addons.tr03124.eac"
		private const val PACE_KEY: String = "org.openecard.sc.pace"
		private const val PCSC_KEY: String = "org.openecard.sc.pcsc"
		private const val HTTPBIND_KEY: String = "org.openecard.control.binding.ktor"

		private fun loadProperties(xmlUtils: XmlUtils): Properties {
			try {
				val confFile: File = LogbackConfig.confFile
				if (!confFile.exists()) {
					// no file yet
					return Properties()
				} else {
					// load file into a Document
					val conf: Document = xmlUtils.w3Builder().str2doc(confFile.inputStream())
					// fill the properties
					val p = Properties()
					p.setProperty(ROOT_KEY, getRootlevel(conf))
					p.setProperty(EID_HTTP_KEY, getLoglevel(conf, EID_HTTP_KEY))
					p.setProperty(EAC_KEY, getLoglevel(conf, EAC_KEY))
					p.setProperty(PACE_KEY, getLoglevel(conf, PACE_KEY))
					p.setProperty(PCSC_KEY, getLoglevel(conf, PCSC_KEY))
					p.setProperty(HTTPBIND_KEY, getLoglevel(conf, HTTPBIND_KEY))
					return p
				}
			} catch (ex: IOException) {
				// something else went wrong
				return Properties()
			} catch (ex: SAXException) {
				return Properties()
			} catch (ex: AddonPropertiesException) {
				return Properties()
			}
		}

		@Throws(AddonPropertiesException::class)
		private fun getLoglevel(
			conf: Document,
			path: String,
		): String {
			try {
				val p: XPath = XPathFactory.newInstance().newXPath()
				val exp: XPathExpression = p.compile(String.format("/configuration/logger[@name='%s']", path))
				val e: Element? = exp.evaluate(conf, XPathConstants.NODE) as Element?

				// no element means NO level
				return e?.getAttribute("level") ?: ""
			} catch (ex: DOMException) {
				throw AddonPropertiesException("Failed to operate on log config document.")
			} catch (ex: XPathExpressionException) {
				throw AddonPropertiesException("Failed to operate on log config document.")
			}
		}

		@Throws(AddonPropertiesException::class)
		private fun setLoglevel(
			conf: Document,
			path: String,
			level: String,
		) {
			try {
				val p: XPath = XPathFactory.newInstance().newXPath()
				val exp: XPathExpression = p.compile(String.format("/configuration/logger[@name='%s']", path))
				var e: Element? = exp.evaluate(conf, XPathConstants.NODE) as Element?

				// create new element
				if (e == null) {
					if (!level.isEmpty()) {
						e = conf.createElement("logger")
						e.setAttribute("name", path)
						e.setAttribute("level", level)
						conf.documentElement.appendChild(e)
					}
					return
				}

				// we have a node let's see what to do next
				if (level.isEmpty()) {
					// delete entry
					e.parentNode.removeChild(e)
				} else {
					// modify level
					e.setAttribute("level", level)
				}
			} catch (ex: DOMException) {
				throw AddonPropertiesException("Failed to operate on log config document.")
			} catch (ex: XPathExpressionException) {
				throw AddonPropertiesException("Failed to operate on log config document.")
			}
		}

		@Throws(AddonPropertiesException::class)
		private fun getRootlevel(conf: Document): String {
			try {
				val p: XPath = XPathFactory.newInstance().newXPath()
				val exp: XPathExpression = p.compile("/configuration/root")
				val e =
					exp.evaluate(conf, XPathConstants.NODE) as Element?
						// no root is beyond our capability
						?: throw AddonPropertiesException("Logging config is invalid, the root element is missing.")

				return e.getAttribute("level")
			} catch (ex: DOMException) {
				throw AddonPropertiesException("Failed to operate on log config document.")
			} catch (ex: XPathExpressionException) {
				throw AddonPropertiesException("Failed to operate on log config document.")
			}
		}

		@Throws(AddonPropertiesException::class)
		private fun setRootlevel(
			conf: Document,
			level: String,
		) {
			try {
				val p: XPath = XPathFactory.newInstance().newXPath()
				val exp: XPathExpression = p.compile("/configuration/root")
				val e =
					exp.evaluate(conf, XPathConstants.NODE) as Element?
						// no root is beyond our capability
						?: throw AddonPropertiesException("Logging config is invalid, the root element is missing.")

				e.setAttribute("level", level)
			} catch (ex: DOMException) {
				throw AddonPropertiesException("Failed to operate on log config document.")
			} catch (ex: XPathExpressionException) {
				throw AddonPropertiesException("Failed to operate on log config document.")
			}
		}
	}
}
