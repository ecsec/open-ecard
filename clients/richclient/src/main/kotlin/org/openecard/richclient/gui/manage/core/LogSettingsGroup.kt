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
import org.openecard.common.I18n
import org.openecard.common.util.FileUtils.resolveResourceAsStream
import org.openecard.richclient.LogbackConfig
import org.openecard.richclient.gui.manage.*
import org.openecard.ws.marshal.WSMarshaller
import org.openecard.ws.marshal.WSMarshallerException
import org.openecard.ws.marshal.WSMarshallerFactory.Companion.createInstance
import org.w3c.dom.DOMException
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.xml.sax.SAXException
import java.awt.Desktop
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.Insets
import java.io.*
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import java.util.*
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JEditorPane
import javax.swing.event.HyperlinkEvent
import javax.swing.event.HyperlinkListener
import javax.swing.text.html.HTMLDocument
import javax.swing.text.html.HTMLEditorKit
import javax.xml.transform.TransformerException
import javax.xml.xpath.*

private val LOG = KotlinLogging.logger {  }

/**
 * Custom settings group for logging settings.
 * The settings are made dynamic to reflect the choice made by the user.
 *
 * @author Tobias Wich
 */
class LogSettingsGroup :
    SettingsGroup(
        lang.translationForKey(GROUP), SettingsFactory.getInstance(
            loadProperties()
        )
    ) {
    init {
        addSelectionItem(
            lang.translationForKey(ROOT_NAME), lang.translationForKey(ROOT_DESC), ROOT_KEY,
            *arrayOf("ERROR", "WARN", "INFO", "DEBUG")
        )
        addLogLevelBox(lang.translationForKey(PAOS_NAME), lang.translationForKey(PAOS_DESC), PAOS_KEY)
        addLogLevelBox(lang.translationForKey(EAC_NAME), lang.translationForKey(EAC_DESC), EAC_KEY)
        addLogLevelBox(lang.translationForKey(PACE_NAME), lang.translationForKey(PACE_DESC), PACE_KEY)
        addLogLevelBox(lang.translationForKey(TRCHECKS_NAME), lang.translationForKey(TRCHECKS_DESC), TRCHECKS_KEY)
        addLogLevelBox(lang.translationForKey(TCTOKEN_NAME), lang.translationForKey(TCTOKEN_DESC), TCTOKEN_KEY)
        addLogLevelBox(lang.translationForKey(EVENT_NAME), lang.translationForKey(EVENT_DESC), EVENT_KEY)
        addLogLevelBox(lang.translationForKey(HTTPBIND_NAME), lang.translationForKey(HTTPBIND_DESC), HTTPBIND_KEY)
        addLogLevelBox(lang.translationForKey(ADDON_NAME), lang.translationForKey(ADDON_DESC), ADDON_KEY)
        addLogLevelBox(lang.translationForKey(SALSTATE_NAME), lang.translationForKey(SALSTATE_DESC), SALSTATE_KEY)
        addLogLevelBox(lang.translationForKey(MDLW_NAME), lang.translationForKey(MDLW_DESC), MDLW_KEY)
        addLogLevelBox(lang.translationForKey(MDLW_EVENT_NAME), lang.translationForKey(MDLW_EVENT_DESC), MDLW_EVENT_KEY)
        addLogLevelBox(lang.translationForKey(CG_NAME), lang.translationForKey(CG_DESC), CG_KEY)

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

    private fun addLogLevelBox(name: String, desc: String, key: String): JComboBox<*> {
        val levels = arrayOf("", "WARN", "INFO", "DEBUG")
        return addSelectionItem(name, desc, key, *levels)
    }

    @Throws(IOException::class, SecurityException::class, AddonPropertiesException::class)
    override fun saveProperties() {
        try {
            val confFile: File = LogbackConfig.confFile

            // create file if needed
            //if (! confFile.exists()) {
            run {
                // overwrite with default settings
                val `is`: InputStream? = resolveResourceAsStream(
                    LogSettingsGroup::class.java, "/logback.xml"
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
    private fun createSupportPanel(): JComponent {
        val kit: HTMLEditorKit = HTMLEditorKit()
        val doc: HTMLDocument = kit.createDefaultDocument() as HTMLDocument

        val editorPane: JEditorPane = JEditorPane()
		editorPane.isEditable = false
        editorPane.setEditorKit(kit)
        editorPane.setDocument(doc)
        //editorPane.setMaximumSize(new Dimension(520, 400));
		editorPane.preferredSize = Dimension(520, 250)

        val url: URL = I18n.getTranslation("richclient").translationForFile("debug", "html")
        editorPane.setPage(url)

        editorPane.addHyperlinkListener(object : HyperlinkListener {
            override fun hyperlinkUpdate(e: HyperlinkEvent) {
                openUrl(e)
            }
        })

        return editorPane
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
						LOG.debug(ex) { "${ex.message}"}
                    }
                }
                if (!browserOpened) {
                    val pb: ProcessBuilder = ProcessBuilder("xdg-open", uri.toString())
                    try {
                        pb.start()
                    } catch (ex: IOException) {
                        // failed to execute command
						LOG.debug(ex) { "${ex.message}"}
                    }
                }
            } catch (ex: URISyntaxException) {
                // wrong syntax
				LOG.debug(ex) { "${ex.message}"}
            }
        }
    }

    companion object {
        private const val serialVersionUID: Long = 1L
        private val lang: I18n = I18n.getTranslation("addon")

        private const val GROUP: String = "addon.core.logging.group_name"
        private const val ROOT_NAME: String = "addon.core.logging.root"
        private const val ROOT_DESC: String = "addon.core.logging.root.desc"
        private const val ROOT_KEY: String = "logging.root"
        private const val PAOS_NAME: String = "addon.core.logging.paos"
        private const val PAOS_DESC: String = "addon.core.logging.paos.desc"
        private const val PAOS_KEY: String = "org.openecard.transport.paos"
        private const val EAC_NAME: String = "addon.core.logging.eac"
        private const val EAC_DESC: String = "addon.core.logging.eac.desc"
        private const val EAC_KEY: String = "org.openecard.sal.protocol.eac"
        private const val PACE_NAME: String = "addon.core.logging.pace"
        private const val PACE_DESC: String = "addon.core.logging.pace.desc"
        private const val PACE_KEY: String = "org.openecard.ifd.protocol.pace"
        private const val TRCHECKS_NAME: String = "addon.core.logging.trchecks"
        private const val TRCHECKS_DESC: String = "addon.core.logging.trchecks.desc"
        private const val TRCHECKS_KEY: String = "org.openecard.common.util.TR03112Utils"
        private const val TCTOKEN_NAME: String = "addon.core.logging.tctoken"
        private const val TCTOKEN_DESC: String = "addon.core.logging.tctoken.desc"
        private const val TCTOKEN_KEY: String = "org.openecard.binding.tctoken"
        private const val EVENT_NAME: String = "addon.core.logging.event"
        private const val EVENT_DESC: String = "addon.core.logging.event.desc"
        private const val EVENT_KEY: String = "org.openecard.event"
        private const val HTTPBIND_NAME: String = "addon.core.logging.httpbind"
        private const val HTTPBIND_DESC: String = "addon.core.logging.httpbind.desc"
        private const val HTTPBIND_KEY: String = "org.openecard.control.binding.http"
        private const val ADDON_NAME: String = "addon.core.logging.addon"
        private const val ADDON_DESC: String = "addon.core.logging.addon.desc"
        private const val ADDON_KEY: String = "org.openecard.addon"
        private const val SALSTATE_NAME: String = "addon.core.logging.salstate"
        private const val SALSTATE_DESC: String = "addon.core.logging.salstate.desc"
        private const val SALSTATE_KEY: String = "org.openecard.common.sal.state"

        private const val MDLW_NAME: String = "addon.core.logging.middleware"
        private const val MDLW_DESC: String = "addon.core.logging.middleware.desc"
        private const val MDLW_KEY: String = "org.openecard.mdlw.sal"
        private const val MDLW_EVENT_NAME: String = "addon.core.logging.middleware-event"
        private const val MDLW_EVENT_DESC: String = "addon.core.logging.middleware-event.desc"
        private const val MDLW_EVENT_KEY: String = "org.openecard.mdlw.event"
        private const val CG_NAME: String = "addon.core.logging.chipgateway"
        private const val CG_DESC: String = "addon.core.logging.chipgateway.desc"
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
        private fun getLoglevel(conf: Document, path: String): String {
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
        private fun setLoglevel(conf: Document, path: String, level: String) {
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
        private fun setRootlevel(conf: Document, level: String) {
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
