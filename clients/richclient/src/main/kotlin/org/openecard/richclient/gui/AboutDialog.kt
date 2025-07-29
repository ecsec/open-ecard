/****************************************************************************
 * Copyright (C) 2012-2025 ecsec GmbH.
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

import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.format
import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.build.BuildInfo
import org.openecard.common.AppVersion
import org.openecard.gui.swing.common.SwingUtils
import org.openecard.i18n.I18N
import org.openecard.richclient.gui.graphics.OecIconType
import org.openecard.richclient.gui.graphics.oecImage
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Font
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.File
import java.net.URI
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

	init {
		setupUI()
	}

	private fun setupUI() {
		val logo = oecImage(OecIconType.COLORED, 147, 147)

		setSize(730, 480)
		// use null layout with absolute positioning
		contentPane.setLayout(null)
		contentPane.setBackground(Color.white)

		val txtpnHeading =
			JTextPane().apply {
				font = Font(Font.SANS_SERIF, Font.BOLD, 20)
				isEditable = false
				text =
					I18N.strings.about_heading
						.format(AppVersion.name)
						.localized()
				setBounds(12, 12, 692, 30)
			}
		contentPane.add(txtpnHeading)

		val txtpnVersion =
			JTextPane().apply {
				font = Font(Font.SANS_SERIF, Font.PLAIN, 9)
				isEditable = false
				text =
					I18N.strings.about_version
						.format(BuildInfo.version)
						.localized()
				setBounds(12, 54, 692, 18)
			}
		contentPane.add(txtpnVersion)

		val label =
			JLabel().apply {
				horizontalAlignment = SwingConstants.CENTER
				icon = ImageIcon(logo)
// 				add(t)
				setBounds(12, 84, 155, 320)
			}
		contentPane.add(label)

		tabbedPane.setBounds(185, 84, 529, 320)
		tabbedPane.background = Color.white

		listOf(
			Triple(ABOUT_TAB, I18N.strings.about_tab_about, I18N.strings.about_html),
			Triple(FEEDBACK_TAB, I18N.strings.about_tab_feedback, I18N.strings.about_feedback_html),
			Triple(LICENSE_TAB, I18N.strings.about_tab_support, I18N.strings.about_support_html),
			Triple(SUPPORT_TAB, I18N.strings.about_tab_license, I18N.strings.about_license_html),
		).forEachIndexed { idx, it ->
			tabbedPane.addTab(it.second.localized(), createTabContent(it.third))
			tabIndices.put(it.first, idx)
		}

		contentPane.add(tabbedPane)

		val btnClose =
			JButton(
				I18N.strings.about_button_close.localized(),
			).apply {
				setBounds(587, 416, 117, 25)
				addActionListener(
					ActionListener { e: ActionEvent? ->
						dispose()
					},
				)
			}

		contentPane.add(btnClose)

		iconImage = logo
		title =
			I18N.strings.about_title
				.format(AppVersion.name)
				.localized()
		defaultCloseOperation = DISPOSE_ON_CLOSE
		isResizable = false
		setLocationRelativeTo(null)
	}

	private fun createTabContent(textResource: StringResource): JPanel {
		val editorPane =
			JEditorPane().apply {
				isEditable = false
				contentType = "text/html"
				text = textResource.localized()
				addHyperlinkListener(
					HyperlinkListener { e ->
						openUrl(e)
					},
				)
			}

		val scrollPane = JScrollPane(editorPane)

		val panel =
			JPanel().apply {
				layout = BorderLayout()
				add(scrollPane, BorderLayout.CENTER)
			}

		return panel
	}

	private fun openUrl(event: HyperlinkEvent) {
		if (event.eventType == HyperlinkEvent.EventType.ACTIVATED) {
			SwingUtils.openUrl(URI.create(event.url.toExternalForm()), true)
		}
	}

	companion object {
		private const val serialVersionUID = 1L

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
		fun showDialog(selectedTab: String = ABOUT_TAB) {
			when (val dialog = runningDialog) {
				null -> {
					runningDialog =
						AboutDialog().apply {
							addWindowListener(
								object : WindowAdapter() {
									override fun windowClosed(e: WindowEvent?) {
										runningDialog = null
									}
								},
							)
							isVisible = true
							selectTab(selectedTab, this)
						}
				}
				else -> {
					dialog.toFront()
					selectTab(selectedTab, dialog)
				}
			}
		}

		// select tab if it exists
		private fun selectTab(
			selectedTab: String,
			dialog: AboutDialog,
		) {
			dialog.tabIndices[selectedTab]?.let {
				dialog.tabbedPane.selectedIndex = it
			} ?: run { LOG.error { "Invalid index selected." } }
		}
	}
}
