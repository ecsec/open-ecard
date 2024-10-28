/****************************************************************************
 * Copyright (C) 2012-2016 ecsec GmbH.
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

package org.openecard.richclient.gui

import io.github.oshai.kotlinlogging.KotlinLogging
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.stage.Stage
import javafx.stage.WindowEvent
import org.openecard.addon.AddonManager
import org.openecard.common.AppVersion.name
import org.openecard.common.I18n
import org.openecard.common.event.EventObject
import org.openecard.common.event.EventType
import org.openecard.common.interfaces.Environment
import org.openecard.common.interfaces.EventCallback
import org.openecard.common.util.ByteUtils
import org.openecard.gui.about.AboutDialog
import org.openecard.richclient.gui.manage.ManagementDialog
import org.openecard.richclient.gui.update.UpdateWindow
import org.openecard.richclient.updater.VersionUpdateChecker
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.*
import java.util.concurrent.ConcurrentSkipListMap
import javax.swing.*
import javax.swing.border.BevelBorder

private val LOG = KotlinLogging.logger {  }

/**
 * This class collects all events published by the EventManager in order to reproduce the current status including the
 * connected card terminals and available cards. It is also responsible for creating the InfoPopup which displays the
 * collected information.
 *
 * @author Johannes Schmölz
 * @author Tobias Wich
 * @author Sebastian Schuberth
 */
class Status(
	private val appTray: AppTray,
	private val env: Environment,
	private val manager: AddonManager
) :
    EventCallback {

    private val lang: I18n = I18n.getTranslation("richclient")

    private val infoMap: MutableMap<String, JPanel?> = ConcurrentSkipListMap()
    private val cardContext: MutableMap<String, ByteArray> = ConcurrentSkipListMap()
    private val cardIcons = HashMap<String, ImageIcon>()
    private var contentPane: JPanel? = null
    private var infoView: JPanel? = null
    private var noTerminal: JPanel? = null
    private var popup: StatusContainer? = null
    private var uw: UpdateWindow? = null
    private var gradPanel: GradientPanel? = null
    private var updateLabel: JLabel? = null

    /**
     * Constructor of Status class.
     *
     * @param appTray tray icon
     * @param env The environment object.
     * @param manager
     */
    init {
        setupBaseUI()
    }

    /**
     * Shows the InfoPopup at the specified position p or at the default if no position is given.
     */
    @JvmOverloads
    fun showInfo(p: Point? = null) {
        if (popup != null && popup is Window) {
            (popup as Window).dispose()
        }
        popup = InfoPopup(contentPane!!, p)
    }

    fun setInfoPanel(frame: StatusContainer?) {
        popup = frame
        popup!!.contentPane = contentPane
    }


    private fun setupBaseUI() {
        contentPane = JPanel()
        contentPane!!.layout = BorderLayout()
        contentPane!!.background = Color.white
        contentPane!!.border = BorderFactory.createBevelBorder(
            BevelBorder.RAISED,
            Color.LIGHT_GRAY,
            Color.DARK_GRAY
        )

        noTerminal = JPanel()
        noTerminal!!.layout = FlowLayout(FlowLayout.LEFT)
        noTerminal!!.background = Color.white
        noTerminal!!.add(createInfoLabel())
        infoMap[NO_TERMINAL_CONNECTED] = noTerminal

        infoView = JPanel()
        infoView!!.layout = BoxLayout(infoView, BoxLayout.PAGE_AXIS)
        infoView!!.background = Color.white
        infoView!!.add(Box.createRigidArea(Dimension(0, 5)))
        infoView!!.add(noTerminal)

        val label = JLabel(" " + lang.translationForKey("tray.title", name) + " ")
        label.font = Font(Font.SANS_SERIF, Font.BOLD, 16)
        label.horizontalAlignment = SwingConstants.CENTER

        gradPanel = GradientPanel(Color(106, 163, 213), Color(80, 118, 177))
        gradPanel!!.layout = BorderLayout()

        gradPanel!!.isOpaque = false
        gradPanel!!.add(label, BorderLayout.CENTER)

        val btnPanel = JPanel()
        btnPanel.layout = FlowLayout(FlowLayout.RIGHT)
        btnPanel.background = Color.white

        val btnExit = JButton(lang.translationForKey("tray.exit"))
        btnExit.addActionListener {
            LOG.debug { "Shutdown button pressed." }
            try {
                appTray.shutdown()
            } catch (ex: Throwable) {
				LOG.error(ex) { "Exiting client threw an error." }
                throw ex
            }
        }

        val btnAbout = JButton(lang.translationForKey("tray.about"))
        btnAbout.addActionListener {
            LOG.debug { "About button pressed." }
            try {
                AboutDialog.showDialog()
            } catch (ex: Throwable) {
				LOG.error(ex) { "Show About dialog threw an error." }
                throw ex
            }
        }

        val btnSettings = JButton(lang.translationForKey("tray.config"))
        btnSettings.addActionListener {
            LOG.debug { "Settings button pressed." }
            try {
                ManagementDialog.Companion.showDialog(manager)
            } catch (ex: Throwable) {
				LOG.error(ex) { "Show Settings dialog threw an error." }
                throw ex
            }
        }

        btnPanel.add(btnSettings)
        btnPanel.add(btnAbout)
        btnPanel.add(btnExit)

        contentPane!!.add(gradPanel, BorderLayout.NORTH)
        contentPane!!.add(infoView, BorderLayout.CENTER)
        contentPane!!.add(btnPanel, BorderLayout.SOUTH)
    }

    @Synchronized
    private fun addInfo(ifdName: String, info: ConnectionHandleType.RecognitionInfo?) {
        if (infoMap.containsKey(NO_TERMINAL_CONNECTED)) {
            infoMap.remove(NO_TERMINAL_CONNECTED)
            infoView!!.removeAll()
        }

        // only add if there is no terminal with an identical name already
        if (infoMap.containsKey(ifdName)) {
            return
        }

        val panel = JPanel()
        panel.layout = FlowLayout(FlowLayout.LEFT)
        panel.add(createInfoLabel(ifdName, info))
        infoMap[ifdName] = panel
        infoView!!.add(panel)

        if (popup != null) {
            popup!!.updateContent(contentPane!!)
        }
    }

    @Synchronized
    private fun updateInfo(ifdName: String, info: ConnectionHandleType.RecognitionInfo?) {
        val panel = infoMap[ifdName]
        if (panel != null) {
            panel.removeAll()
            panel.add(createInfoLabel(ifdName, info))
            panel.repaint()

            if (popup != null) {
                popup!!.updateContent(contentPane!!)
            }
        }
    }

    @Synchronized
    private fun removeInfo(ifdName: String) {
        val panel = infoMap[ifdName]
        if (panel != null) {
            infoMap.remove(ifdName)
            infoView!!.remove(panel)

            if (infoMap.isEmpty()) {
                infoMap[NO_TERMINAL_CONNECTED] = noTerminal
                infoView!!.add(noTerminal)
            }

            if (popup != null) {
                popup!!.updateContent(contentPane!!)
            }
        }
    }

    @Synchronized
    private fun getCardIcon(cardType: String?): ImageIcon? {
        var cardType = cardType
        if (cardType == null) {
            cardType = "http://openecard.org/cif/no-card"
        }

        if (!cardIcons.containsKey(cardType)) {
            var `is` = env.recognition.getCardImage(cardType)
				?: env.recognition.unknownCardImage
            val icon = GuiUtils.getScaledCardImageIcon(`is`)
            cardIcons[cardType] = icon
        }

        return cardIcons[cardType]
    }

    private fun getCardType(info: ConnectionHandleType.RecognitionInfo?): String {
        if (info != null) {
            val cardType = info.cardType

            return if (cardType != null) {
                resolveCardType(cardType)
            } else {
                lang.translationForKey("status.nocard")
            }
        } else {
            return lang.translationForKey("status.nocard")
        }
    }

    private fun resolveCardType(cardType: String): String {
        if (cardType == "http://bsi.bund.de/cif/unknown") {
            return lang.translationForKey("status.unknowncard")
        } else {
            // read CardTypeName from CardInfo file
            var cardTypeName = cardType
            val cif = env.cifProvider.getCardInfo(cardType)

            if (cif != null) {
                val type = cif.cardType
                if (type != null) {
                    var found = false
                    val languages = arrayOf(Locale.getDefault().language, "en")

                    // check native lang, then english
                    for (language in languages) {
                        if (found) { // stop when the inner loop terminated
                            break
                        }

                        val cardTypeNames = type.cardTypeName
                        for (ist in cardTypeNames) {
                            if (ist.lang.equals(language, ignoreCase = true)) {
                                cardTypeName = ist.value
                                found = true
                                break
                            }
                        }
                    }
                }
            }

            return cardTypeName
        }
    }

    private fun createInfoLabel(ifdName: String? = null, info: ConnectionHandleType.RecognitionInfo? = null): JLabel {
        val label = JLabel()

        if (ifdName != null) {
            val cardType = if (info != null) info.cardType else "http://openecard.org/cif/no-card"
            label.icon = getCardIcon(cardType)
            label.text = "<html><b>" + getCardType(info) + "</b><br><i>" + ifdName + "</i></html>"
        } else {
            // no_terminal.png is based on klaasvangend_USB_plug.svg by klaasvangend
            // see: http://openclipart.org/detail/3705/usb-plug-by-klaasvangend
            label.icon = getCardIcon("http://openecard.org/cif/no-terminal")
            label.text = "<html><i>" + lang.translationForKey("status.noterminal") + "</i></html>"
        }

        label.iconTextGap = 10
        label.background = Color.white

        // on Windows the label width is too small to display all information
        val dim = label.preferredSize
        label.preferredSize = Dimension(dim.width + 10, dim.height)

        return label
    }

    @Synchronized
    override fun signalEvent(eventType: EventType?, eventData: EventObject) {
		LOG.debug { "Event: $eventType" }

        val ch = eventData.handle
        if (ch == null) {
			LOG.error { "No handle provided in event ${eventType}." }
            return
        }

        val ifdName = ch.ifdName
        val ctx = ch.contextHandle
		LOG.debug { "ConnectionHandle: ifd=${ifdName}, slot=${ByteUtils.toHexString(ch.slotHandle)}, ctx=${ByteUtils.toHexString(ctx)}" }
        val info = ch.recognitionInfo
        if (info != null) {
			LOG.debug { "RecognitionInfo: ${info.cardType}, ${ByteUtils.toHexString(info.cardIdentifier)}" }
        } else {
			LOG.debug { "RecognitionInfo: null" }
        }

        if (null != eventType && isResponsibleContext(ifdName, ctx)) {
            when (eventType) {
                EventType.TERMINAL_ADDED -> addInfo(ifdName, info)
                EventType.TERMINAL_REMOVED -> {
                    removeInfo(ifdName)
                    removeResponsibleContext(ifdName)
                    updateInfo(ifdName, info)
                }

                EventType.CARD_REMOVED -> {
                    removeResponsibleContext(ifdName)
                    updateInfo(ifdName, info)
                }

                EventType.CARD_RECOGNIZED -> {
                    setResponsibleContext(ifdName, ctx)
                    addInfo(ifdName, info)
                    updateInfo(ifdName, info)
                }

                EventType.CARD_INSERTED -> {
                    addInfo(ifdName, info)
                    updateInfo(ifdName, info)
                }

				else -> {}
            }
        }
    }

    private fun isResponsibleContext(ifd: String, ctx: ByteArray): Boolean {
        val isResponsible = ByteUtils.compare(ctx, cardContext.getOrDefault(ifd, ctx))
		LOG.debug { "Event sent has responsibility=${isResponsible} for this card." }
        return isResponsible
    }

    private fun setResponsibleContext(ifd: String, ctx: ByteArray) {
        cardContext[ifd] = ByteUtils.clone(ctx)
    }

    private fun removeResponsibleContext(ifd: String) {
        cardContext.remove(ifd)
    }

    fun showUpdateIcon(checker: VersionUpdateChecker) {
        if (updateLabel != null) {
            gradPanel!!.remove(updateLabel)
        }

        val img = GuiUtils.getImage("update_available.png")
        val resizedImage = img.getScaledInstance(40, 40, 0)
        val icon: Icon = ImageIcon(resizedImage)
        updateLabel = JLabel(icon)

        updateLabel!!.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                openUpdateWindow(checker)
            }
        })

        gradPanel!!.add(updateLabel, BorderLayout.EAST)

        if (popup != null) {
            popup!!.updateContent(contentPane!!)
        }
    }

    @Synchronized
    private fun openUpdateWindow(checker: VersionUpdateChecker) {
        if (uw == null) {
            // no window displayed, start it up	    

            Platform.runLater {
                val stage = Stage()
                stage.onHidden =
                    EventHandler { event: WindowEvent? ->
                        synchronized(this) {
                            uw = null
                        }
                    }
                uw = UpdateWindow(checker, stage)
                uw!!.init()
            }
        } else {
            // window is already displayed, just bring it to the front
            Platform.runLater { uw!!.toFront() }
        }
    }

}

private const val NO_TERMINAL_CONNECTED = "noTerminalConnected"
