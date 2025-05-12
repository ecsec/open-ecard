/****************************************************************************
 * Copyright (C) 2013-2025 ecsec GmbH.
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

package org.openecard.richclient.gui.manage

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.addon.AddonPropertiesException
import org.openecard.common.I18n
import org.openecard.richclient.gui.graphics.OecIconType
import org.openecard.richclient.gui.graphics.oecImage
import java.awt.AlphaComposite
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics2D
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.IOException
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSeparator
import javax.swing.JTabbedPane
import javax.swing.ScrollPaneConstants
import javax.swing.SwingConstants
import javax.swing.border.Border
import javax.swing.border.EmptyBorder

private val logger = KotlinLogging.logger { }

/**
 * Base implementation of an add-on panel.
 * `AddonPanel`s are used to represent add-ons on the [ManagementDialog]. This implementation is complete,
 * however it should be subclassed to reflect the needs of the add-on or builtin item.<br></br>
 * `AddonPanel`s are either arranged as tabs, or as a single content panel, depending on the use case.
 *
 * @author Tobias Wich
 */
open class AddonPanel : JPanel {
	private val lang: I18n = I18n.getTranslation("addon")

	private var logo: Image?
	private var settingsPanel: SettingsPanel? = null

	/**
	 * Creates an AddonPanel with the given panels.
	 * Each of the panel types may be left out, but at least one must be present in order for the dialog to make any
	 * sense. The panels are arranged in a tab pane.
	 *
	 * @param actionPanel Optional action panel of the add-on.
	 * @param settingsPanel Optional settings panel of the add-on.
	 * @param aboutPanel Optional about page panel of the add-on.
	 * @param name Name of the add-on as displayed in the head of the panel.
	 * @param description Optional description of the add-on as displayed in the head of the panel.
	 * @param logo Optional logo of the add-on as displayed on the [ManagementDialog].
	 * If not present a default will be used.
	 */
	constructor(
		actionPanel: ActionPanel?,
		settingsPanel: SettingsPanel?,
		aboutPanel: AboutPanel?,
		name: String,
		description: String?,
		logo: Image?,
	) {
		setLayout(BorderLayout(0, 0))

		this.logo = logo
		val tabbedPane: JTabbedPane = JTabbedPane(JTabbedPane.TOP)
		add(tabbedPane)
		val dim: Dimension = Dimension(100, 100)

		if (actionPanel != null) {
			val actionScrollPane: JScrollPane = JScrollPane(actionPanel)
			actionScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)
			actionScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED)
			actionScrollPane.setMinimumSize(dim)
			actionScrollPane.setPreferredSize(dim)
			actionScrollPane.setBorder(EMPTY_BORDER)
			tabbedPane.addTab(lang.translationForKey("addon.panel.tab.function"), null, actionScrollPane, null)
		}
		if (settingsPanel != null) {
			this.settingsPanel = settingsPanel
			tabbedPane.addTab(lang.translationForKey("addon.panel.tab.settings"), null, settingsPanel, null)
		}
		if (aboutPanel != null) {
			tabbedPane.addTab(lang.translationForKey("addon.panel.tab.about"), null, aboutPanel, null)
		}

		createHeader(name, description)
	}

	/**
	 * Creates an AddonPanel with the given panel.
	 * The panels is placed directly on this panel.
	 *
	 * @param singlePanel Panel to display. This panel may be of any type.
	 * @param name Name of the add-on as displayed in the head of the panel.
	 * @param description Optional description of the add-on as displayed in the head of the panel.
	 * @param logo Optional logo of the add-on as displayed on the [ManagementDialog].
	 * If not present a default will be used.
	 */
	constructor(
		singlePanel: JPanel,
		name: String,
		description: String?,
		logo: Image?,
	) {
		setLayout(BorderLayout(0, 0))
		this.logo = logo
		createHeader(name, description)

		val panel: JComponent
		if (singlePanel !is AboutPanel && singlePanel !is SettingsPanel) {
			val singleScrollPane: JScrollPane = JScrollPane(singlePanel)
			singleScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER)
			singleScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED)
			val dim: Dimension = Dimension(this.getWidth(), this.getHeight() - 75)
			singleScrollPane.setMinimumSize(dim)
			singleScrollPane.setPreferredSize(dim)
			singleScrollPane.setBorder(EMPTY_BORDER)
			panel = singleScrollPane
		} else {
			panel = singlePanel
		}

		if (singlePanel is SettingsPanel) {
			this.settingsPanel = singlePanel
		}

		add(panel)
	}

	/**
	 * Saves the properties of the settings panel if one is present.
	 */
	fun saveProperties() {
		try {
			if (settingsPanel != null) {
				settingsPanel!!.saveProperties()
			}
		} catch (ex: IOException) {
			logger.error(ex) { "Failed to save settings." }
		} catch (ex: SecurityException) {
			logger.error(ex) { "Missing permissions to save settings." }
		} catch (ex: AddonPropertiesException) {
			logger.error(ex) { "Failed to save addon settings." }
		}
	}

	/**
	 * Gets the logo of the add-on.
	 * If no logo has been defined, a default logo is returned.
	 *
	 * @return The logo of the add-on.
	 */
	fun getLogo(): Image = logo?.getScaledInstance(LOGO_WIDTH, LOGO_HEIGHT, Image.SCALE_SMOOTH) ?: defaultLogo

	private fun createHeader(
		name: String,
		description: String?,
	) {
		val panel: Box = Box.createVerticalBox()
		add(panel, BorderLayout.NORTH)

		val content: Box = Box.createVerticalBox()
		content.setAlignmentX(LEFT_ALIGNMENT)
		content.setBorder(EmptyBorder(5, 20, 0, 0))
		content.minimumSize = Dimension(50, 50)
		content.preferredSize = Dimension(50, 50)
		panel.add(content)

		val nameLabel: JLabel = JLabel(name)
		nameLabel.setAlignmentX(LEFT_ALIGNMENT)
		nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 19f))
		nameLabel.setForeground(Color.getHSBColor(0f, 0f, 0.25f))
		content.add(nameLabel)

		if (description != null && !description.isEmpty()) {
			content.add(Box.createVerticalStrut(5))
			val descLabel: JLabel = JLabel(description)
			descLabel.setAlignmentX(LEFT_ALIGNMENT)
			descLabel.setFont(descLabel.getFont().deriveFont(Font.PLAIN))
			descLabel.setForeground(Color.getHSBColor(0f, 0f, 0.25f))
			content.add(descLabel)
		}

		panel.add(Box.createVerticalStrut(4))

		val rule: JSeparator = JSeparator(SwingConstants.HORIZONTAL)
		rule.setBorder(EmptyBorder(10, 20, 10, 20))
		rule.setAlignmentX(LEFT_ALIGNMENT)
		panel.add(rule)

		panel.add(Box.createVerticalStrut(5))
	}

	private val defaultLogo: Image
		get() {
			val original: Image =
				oecImage(
					OecIconType.COLORED,
					LOGO_WIDTH,
					LOGO_HEIGHT,
				)
			val result: BufferedImage =
				BufferedImage(
					LOGO_WIDTH,
					LOGO_HEIGHT,
					BufferedImage.TYPE_INT_ARGB,
				)
			val g: Graphics2D = result.createGraphics()

			// draw original image with grey shade
			g.composite = AlphaComposite.SrcOver.derive(0.3f)
			g.drawImage(original, 0, 0, null)

			g.dispose()
			return result
		}

	companion object {
		private const val serialVersionUID: Long = 1L

		private const val LOGO_WIDTH: Int = 45
		private const val LOGO_HEIGHT: Int = 45

		private val EMPTY_BORDER: Border = BorderFactory.createEmptyBorder()
	}
}
