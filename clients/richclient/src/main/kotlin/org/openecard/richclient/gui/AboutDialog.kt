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
import org.openecard.common.AppVersion
import org.openecard.common.AppVersion.version
import org.openecard.common.I18n
import org.openecard.common.util.StringUtils
import org.openecard.gui.graphics.GraphicsUtil.createImage
import org.openecard.gui.graphics.OecLogo
import org.openecard.gui.swing.common.SwingUtils
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Font
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.WindowEvent
import java.awt.event.WindowListener
import java.io.File
import java.io.IOException
import java.net.URI
import java.net.URL
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JEditorPane
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTabbedPane
import javax.swing.JTextPane
import javax.swing.SwingConstants
import javax.swing.event.HyperlinkEvent
import javax.swing.event.HyperlinkListener
import javax.swing.text.html.HTMLDocument
import javax.swing.text.html.HTMLEditorKit
import kotlin.jvm.java

private val LOG = KotlinLogging.logger { }

/**
 * This class is used to create a Swing based about dialog.
 * The dialog is localized with the `about` properties from the i18n module and the HTML pages in this modules'
 * `openecard_i18n/about` directory.
 *
 * @author Johannes Schmölz
 * @author Tobias Wich
 */
class AboutDialog private constructor() : JFrame() {
	private val tabIndices = mutableMapOf<String, Int>()
	private var tabbedPane = JTabbedPane(JTabbedPane.TOP)

	/**
	 * Creates a new instance of this class.
	 */
	init {
		setupUI()
	}

	private fun setupUI() {
		val logo = createImage(OecLogo::class.java, 147, 147)

		setSize(730, 480)
		// use null layout with absolute positioning
		contentPane.setLayout(null)
		contentPane.setBackground(Color.white)

		val txtpnHeading = JTextPane()
		txtpnHeading.setFont(Font(Font.SANS_SERIF, Font.BOLD, 20))
		txtpnHeading.isEditable = false
		txtpnHeading.text = LANG.translationForKey("about.heading", AppVersion.name)
		txtpnHeading.setBounds(12, 12, 692, 30)
		contentPane.add(txtpnHeading)

		val txtpnVersion = JTextPane()
		txtpnVersion.setFont(Font(Font.SANS_SERIF, Font.PLAIN, 9))
		txtpnVersion.isEditable = false
		txtpnVersion.text = LANG.translationForKey("about.version", version)
		txtpnVersion.setBounds(12, 54, 692, 18)
		contentPane.add(txtpnVersion)

		val label = JLabel()
		label.setHorizontalAlignment(SwingConstants.CENTER)
		label.setIcon(ImageIcon(logo))
		label.setBounds(12, 84, 155, 320)
		contentPane.add(label)

		tabbedPane.setBounds(185, 84, 529, 320)
		tabbedPane.setBackground(Color.white)
		var tabIdx = 0
		tabbedPane.addTab(LANG.translationForKey("about.tab.about"), createTabContent(ABOUT_TAB))
		tabIndices.put(ABOUT_TAB, tabIdx++)
		tabbedPane.addTab(LANG.translationForKey("about.tab.feedback"), createTabContent(FEEDBACK_TAB))
		tabIndices.put(FEEDBACK_TAB, tabIdx++)
		tabbedPane.addTab(LANG.translationForKey("about.tab.support"), createTabContent(SUPPORT_TAB))
		tabIndices.put(SUPPORT_TAB, tabIdx++)
		tabbedPane.addTab(LANG.translationForKey("about.tab.license"), createTabContent(LICENSE_TAB))
		tabIndices.put(LICENSE_TAB, tabIdx++)
		contentPane.add(tabbedPane)

		val btnClose: JButton = JButton(LANG.translationForKey("about.button.close"))
		btnClose.setBounds(587, 416, 117, 25)
		btnClose.addActionListener(
			ActionListener { e: ActionEvent? ->
				dispose()
			},
		)
		contentPane.add(btnClose)

		iconImage = logo
		setTitle(LANG.translationForKey("about.title", AppVersion.name))
		setDefaultCloseOperation(DISPOSE_ON_CLOSE)
		setResizable(false)
		setLocationRelativeTo(null)
	}

	private fun createTabContent(resourceName: String?): JPanel {
		val kit = HTMLEditorKit()
		kit.isAutoFormSubmission = false // don't follow form link, use hyperlink handler instead
		val doc = kit.createDefaultDocument() as HTMLDocument?

		val editorPane = JEditorPane()
		editorPane.isEditable = false
		editorPane.setEditorKit(kit)
		editorPane.setDocument(doc)

		try {
			val url: URL = LANG.translationForFile(resourceName, "html")
			editorPane.setPage(url)
		} catch (ex: IOException) {
			editorPane.text = "Page not found."
		}

		editorPane.addHyperlinkListener(
			HyperlinkListener { e ->
				openUrl(e)
			},
		)

		val scrollPane = JScrollPane(editorPane)

		val panel = JPanel()
		panel.setLayout(BorderLayout())
		panel.add(scrollPane, BorderLayout.CENTER)

		return panel
	}

	private fun openUrl(event: HyperlinkEvent) {
		val type = event.eventType
		if (type == HyperlinkEvent.EventType.ACTIVATED) {
			val url = event.url
			SwingUtils.openUrl(URI.create(url.toExternalForm()), true)
		}
	}

	companion object {
		private const val serialVersionUID = 1L
		private val LANG: I18n = I18n.getTranslation("about")

		const val ABOUT_TAB: String = "about"
		const val FEEDBACK_TAB: String = "feedback"
		const val LICENSE_TAB: String = "license"
		const val SUPPORT_TAB: String = "support"

		private var runningDialog: AboutDialog? = null

		init {
			try {
				// create user.home.url property
				val userHome = System.getProperty("user.home")
				val f = File(userHome)
				// strip file:// as this must be written in the html file
				val userHomeUrl = f.toURI().toString().substring(5)
				LOG.debug { "user.home.url = $userHomeUrl" }
				System.setProperty("user.home.url", userHomeUrl)
			} catch (ex: SecurityException) {
				LOG.error(ex) { "Failed to calculate property 'user.home.url'." }
			}
		}

		/**
		 * Shows an about dialog and selects the specified index.
		 * This method makes sure, that there is only one about dialog.
		 *
		 * @param selectedTab The identifier of the tab which should be selected. Valid identifiers are defined as constants
		 * in this class.
		 */
		@JvmStatic
		@JvmOverloads
		fun showDialog(selectedTab: String? = ABOUT_TAB) {
			if (runningDialog == null) {
				val dialog = AboutDialog()
				dialog.addWindowListener(
					object : WindowListener {
						override fun windowOpened(e: WindowEvent?) {}

						override fun windowClosing(e: WindowEvent?) {}

						override fun windowClosed(e: WindowEvent?) {
							runningDialog = null
						}

						override fun windowIconified(e: WindowEvent?) {}

						override fun windowDeiconified(e: WindowEvent?) {}

						override fun windowActivated(e: WindowEvent?) {}

						override fun windowDeactivated(e: WindowEvent?) {}
					},
				)
				dialog.isVisible = true
				runningDialog = dialog
			} else {
				runningDialog!!.toFront()
			}

			// select tab if it exists
			val idx = runningDialog!!.tabIndices[StringUtils.nullToEmpty(selectedTab)]
			if (idx != null) {
				try {
					runningDialog!!.tabbedPane.setSelectedIndex(idx)
				} catch (ex: ArrayIndexOutOfBoundsException) {
					LOG.error { "Invalid index selected." }
				}
			}
		}
	}
}
