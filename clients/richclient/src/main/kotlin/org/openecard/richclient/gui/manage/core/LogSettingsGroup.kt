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
 ***************************************************************************/

package org.openecard.richclient.gui.manage.core

import ch.qos.logback.core.joran.spi.JoranException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.addon.AddonPropertiesException
import org.openecard.common.util.FileUtils.resolveResourceAsStream
import org.openecard.i18n.I18N
import org.openecard.richclient.LogbackConfig
import org.openecard.richclient.gui.manage.SettingsFactory
import org.openecard.richclient.gui.manage.SettingsGroup
import org.openecard.ws.marshal.WSMarshaller
import org.openecard.ws.marshal.WSMarshallerException
import org.openecard.ws.marshal.WSMarshallerFactory.Companion.createInstance
import org.w3c.dom.DOMException
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.xml.sax.SAXException
import java.awt.Desktop
import java.awt.GridBagConstraints
import java.awt.Insets
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.net.URISyntaxException
import java.util.Locale
import java.util.Properties
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JEditorPane
import javax.swing.event.HyperlinkEvent
import javax.swing.event.HyperlinkListener
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
class LogSettingsGroup :
	SettingsGroup(
		I18N.strings.addon_core_logging_group_name.localized(),
		SettingsFactory.getInstance(
			loadProperties(),
		),
	) {
	init {
		addSelectionItem(
			I18N.strings.addon_core_logging_root.localized(),
			I18N.strings.addon_core_logging_root_desc.localized(),
			ROOT_KEY,
			*arrayOf("ERROR", "WARN", "INFO", "DEBUG"),
		)
		addLogLevelBox(
			I18N.strings.addon_core_logging_paos.localized(),
			I18N.strings.addon_core_logging_paos_desc.localized(),
			PAOS_KEY,
		)
		addLogLevelBox(
			I18N.strings.addon_core_logging_eac.localized(),
			I18N.strings.addon_core_logging_eac_desc.localized(),
			EAC_KEY,
		)
		addLogLevelBox(
			I18N.strings.addon_core_logging_pace.localized(),
			I18N.strings.addon_core_logging_pace_desc.localized(),
			PACE_KEY,
		)
		addLogLevelBox(
			I18N.strings.addon_core_logging_trchecks.localized(),
			I18N.strings.addon_core_logging_trchecks_desc.localized(),
			TRCHECKS_KEY,
		)
		addLogLevelBox(
			I18N.strings.addon_core_logging_tctoken.localized(),
			I18N.strings.addon_core_logging_tctoken_desc.localized(),
			TCTOKEN_KEY,
		)
		addLogLevelBox(
			I18N.strings.addon_core_logging_event.localized(),
			I18N.strings.addon_core_logging_event_desc.localized(),
			EVENT_KEY,
		)
		addLogLevelBox(
			I18N.strings.addon_core_logging_httpbind.localized(),
			I18N.strings.addon_core_logging_httpbind_desc.localized(),
			HTTPBIND_KEY,
		)
		addLogLevelBox(
			I18N.strings.addon_core_logging_addon.localized(),
			I18N.strings.addon_core_logging_addon_desc.localized(),
			ADDON_KEY,
		)
		addLogLevelBox(
			I18N.strings.addon_core_logging_salstate.localized(),
			I18N.strings.addon_core_logging_salstate_desc.localized(),
			SALSTATE_KEY,
		)
		addLogLevelBox(
			I18N.strings.addon_core_logging_middleware.localized(),
			I18N.strings.addon_core_logging_middleware_desc.localized(),
			MDLW_KEY,
		)
		addLogLevelBox(
			I18N.strings.addon_core_logging_middleware.localized(),
			I18N.strings.addon_core_logging_middleware.localized(),
			MDLW_EVENT_KEY,
		)
		addLogLevelBox(
			I18N.strings.addon_core_logging_chipgateway.localized(),
			I18N.strings.addon_core_logging_chipgateway_desc.localized(),
			CG_KEY,
		)

		// add support text
		try {
			val panel: JComponent = createSupportPanel()

			val constraints: GridBagConstraints = GridBagConstraints()
			constraints.insets = Insets(5, 10, 0, 5)
			constraints.fill = GridBagConstraints.NONE
			constraints.gridheight = GridBagConstraints.RELATIVE
			constraints.gridwidth = GridBagConstraints.RELATIVE
			constraints.gridx = 0
			constraints.gridy = itemIdx++
			constraints.anchor = GridBagConstraints.WEST
			container.add(panel, constraints)
		} catch (ex: IOException) {
			// no support panel text available
		}
	}

	private fun addLogLevelBox(
		name: String,
		desc: String,
		key: String,
	): JComboBox<*> {
		val levels = arrayOf("", "WARN", "INFO", "DEBUG")
		return addSelectionItem(name, desc, key, *levels)
	}

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
			val m: WSMarshaller = createInstance()
			m.removeAllTypeClasses()
			val fin: FileInputStream = FileInputStream(confFile)
			val conf: Document = m.str2doc(fin)

			// process root value
			var value: String? = properties.getProperty(ROOT_KEY)
			value = value ?: "ERROR"
			setRootlevel(conf, value)
			// process every value
			value = properties.getProperty(PAOS_KEY)
			value = value ?: ""
			setLoglevel(conf, PAOS_KEY, value)
			value = properties.getProperty(EAC_KEY)
			value = value ?: ""
			setLoglevel(conf, EAC_KEY, value)
			value = properties.getProperty(PACE_KEY)
			value = value ?: ""
			setLoglevel(conf, PACE_KEY, value)
			value = properties.getProperty(TRCHECKS_KEY)
			value = value ?: ""
			setLoglevel(conf, TRCHECKS_KEY, value)
			value = properties.getProperty(TCTOKEN_KEY)
			value = value ?: ""
			setLoglevel(conf, TCTOKEN_KEY, value)
			value = properties.getProperty(EVENT_KEY)
			value = value ?: ""
			setLoglevel(conf, EVENT_KEY, value)
			value = properties.getProperty(HTTPBIND_KEY)
			value = value ?: ""
			setLoglevel(conf, HTTPBIND_KEY, value)
			value = properties.getProperty(ADDON_KEY)
			value = value ?: ""
			setLoglevel(conf, ADDON_KEY, value)
			value = properties.getProperty(SALSTATE_KEY)
			value = value ?: ""
			setLoglevel(conf, SALSTATE_KEY, value)
			value = properties.getProperty(MDLW_KEY)
			value = value ?: ""
			setLoglevel(conf, MDLW_KEY, value)
			value = properties.getProperty(MDLW_EVENT_KEY)
			value = value ?: ""
			setLoglevel(conf, MDLW_EVENT_KEY, value)
			value = properties.getProperty(CG_KEY)
			value = value ?: ""
			setLoglevel(conf, CG_KEY, value)
			FileWriter(confFile).use { w ->
				val confStr: String = m.doc2str(conf)
				w.write(confStr)
			}
			// reload log config
			LogbackConfig.load()
		} catch (ex: JoranException) {
			throw AddonPropertiesException(ex.message!!, ex)
		} catch (ex: WSMarshallerException) {
			throw IOException(ex.message, ex)
		} catch (ex: SAXException) {
			throw IOException(ex.message, ex)
		} catch (ex: TransformerException) {
			throw IOException(ex.message, ex)
		}
	}

	@Throws(IOException::class)
	private fun createSupportPanel(): JComponent =
		JEditorPane().apply {
			isEditable = false
			contentType = "text/html"
			text =
				org.openecard.i18n.helper
					.getFileResource("debug", Locale.getDefault().language)
					?.readText() ?: "File resource not found"
			addHyperlinkListener(
				HyperlinkListener { e ->
					openUrl(e)
				},
			)
		}

	private fun openUrl(event: HyperlinkEvent) {
		val type: HyperlinkEvent.EventType = event.eventType
		if (type == HyperlinkEvent.EventType.ACTIVATED) {
			val url: String = event.url.toExternalForm()
			try {
				var browserOpened: Boolean = false
				val uri: URI = URI(url)
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
					val pb: ProcessBuilder = ProcessBuilder("xdg-open", uri.toString())
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
		private const val PAOS_KEY: String = "org.openecard.transport.paos"
		private const val EAC_KEY: String = "org.openecard.sal.protocol.eac"
		private const val PACE_KEY: String = "org.openecard.ifd.protocol.pace"
		private const val TRCHECKS_KEY: String = "org.openecard.common.util.TR03112Utils"
		private const val TCTOKEN_KEY: String = "org.openecard.binding.tctoken"
		private const val EVENT_KEY: String = "org.openecard.event"
		private const val HTTPBIND_KEY: String = "org.openecard.control.binding.http"
		private const val ADDON_KEY: String = "org.openecard.addon"
		private const val SALSTATE_KEY: String = "org.openecard.common.sal.state"
		private const val MDLW_KEY: String = "org.openecard.mdlw.sal"
		private const val MDLW_EVENT_KEY: String = "org.openecard.mdlw.event"
		private const val CG_KEY: String = "org.openecard.addons.cg"

		private fun loadProperties(): Properties {
			try {
				val confFile: File = LogbackConfig.confFile
				if (!confFile.exists()) {
					// no file yet
					return Properties()
				} else {
					// load file into a Document
					val m: WSMarshaller = createInstance()
					m.removeAllTypeClasses()
					val fin = FileInputStream(confFile)
					val conf: Document = m.str2doc(fin)
					// fill the properties
					val p = Properties()
					p.setProperty(ROOT_KEY, getRootlevel(conf))
					p.setProperty(PAOS_KEY, getLoglevel(conf, PAOS_KEY))
					p.setProperty(EAC_KEY, getLoglevel(conf, EAC_KEY))
					p.setProperty(PACE_KEY, getLoglevel(conf, PACE_KEY))
					p.setProperty(TRCHECKS_KEY, getLoglevel(conf, TRCHECKS_KEY))
					p.setProperty(TCTOKEN_KEY, getLoglevel(conf, TCTOKEN_KEY))
					p.setProperty(EVENT_KEY, getLoglevel(conf, EVENT_KEY))
					p.setProperty(HTTPBIND_KEY, getLoglevel(conf, HTTPBIND_KEY))
					p.setProperty(ADDON_KEY, getLoglevel(conf, ADDON_KEY))
					p.setProperty(SALSTATE_KEY, getLoglevel(conf, SALSTATE_KEY))
					p.setProperty(MDLW_KEY, getLoglevel(conf, MDLW_KEY))
					p.setProperty(MDLW_EVENT_KEY, getLoglevel(conf, MDLW_EVENT_KEY))
					p.setProperty(CG_KEY, getLoglevel(conf, CG_KEY))
					return p
				}
			} catch (ex: IOException) {
				// something else went wrong
				return Properties()
			} catch (ex: SAXException) {
				return Properties()
			} catch (ex: WSMarshallerException) {
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
				val e: Element? = exp.evaluate(conf, XPathConstants.NODE) as Element?
				// no root is beyond our capability
				if (e == null) {
					throw AddonPropertiesException("Logging config is invalid, the root element is missing.")
				}

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
				val e: Element? = exp.evaluate(conf, XPathConstants.NODE) as Element?
				// no root is beyond our capability
				if (e == null) {
					throw AddonPropertiesException("Logging config is invalid, the root element is missing.")
				}

				e.setAttribute("level", level)
			} catch (ex: DOMException) {
				throw AddonPropertiesException("Failed to operate on log config document.")
			} catch (ex: XPathExpressionException) {
				throw AddonPropertiesException("Failed to operate on log config document.")
			}
		}
	}
}
