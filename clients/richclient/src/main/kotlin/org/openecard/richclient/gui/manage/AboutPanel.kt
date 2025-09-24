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

package org.openecard.richclient.gui.manage

import org.openecard.i18n.I18N
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.GridLayout
import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import javax.swing.BorderFactory
import javax.swing.ButtonGroup
import javax.swing.JEditorPane
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.JScrollPane
import javax.swing.ScrollPaneConstants

/**
 * Implements an about panel which contains basic information about the installed addon.
 *
 * @author Hans-Martin Haase
 */
class AboutPanel(
	coreAddon: Boolean,
	dialog: ManagementDialog,
) : JPanel() {
	private val layout: GridBagLayout = GridBagLayout()
	private val license: String
	private val about: String
	private val dialog: ManagementDialog
	private var display: JEditorPane? = null

	/**
	 * Setup the header which contains basic information about license and the version.
	 */
	private fun setupHead() {
		val basePane = JPanel(GridBagLayout())
		basePane.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2))
		basePane.setLayout(GridLayout(2, 1))
		val versionLabel = JLabel(I18N.strings.about_version.localized() + ":")
		val licenseLabel = JLabel(I18N.strings.addon_about_license_type.localized() + ":")

		val lc = GridBagConstraints()
		lc.anchor = GridBagConstraints.WEST
		lc.gridx = 0
		lc.gridy = 0
		lc.weightx = 1.0
		lc.weighty = 1.0
		basePane.add(versionLabel, lc)

		val lc2 = GridBagConstraints()
		lc2.anchor = GridBagConstraints.WEST
		lc2.fill = GridBagConstraints.HORIZONTAL
		lc2.gridwidth = GridBagConstraints.REMAINDER
		lc2.gridx = 1
		lc2.gridy = 0
		lc2.weightx = 1.0
		lc2.weighty = 1.0
		// basePane.add(JLabel(addonSpec.getVersion()))
		basePane.add(JLabel("TODO: Addon Version"))

		val lc3 = GridBagConstraints()
		lc3.anchor = GridBagConstraints.WEST
		lc3.gridx = 1
		lc3.gridy = 2
		lc3.weightx = 1.0
		lc3.weighty = 1.0
		basePane.add(licenseLabel, lc3)

		val lc4 = GridBagConstraints()
		lc4.anchor = GridBagConstraints.WEST
		lc4.fill = GridBagConstraints.HORIZONTAL
		lc4.gridwidth = GridBagConstraints.REMAINDER
		lc4.gridx = 1
		lc4.gridy = 0
		lc4.weightx = 1.0
		lc4.weighty = 1.0
		// basePane.add(JLabel(addonSpec.license))
		basePane.add(JLabel("TODO: Addon License"))

		val c = GridBagConstraints()
		c.fill = GridBagConstraints.HORIZONTAL
		c.gridwidth = GridBagConstraints.REMAINDER
		c.weightx = 1.0
		c.weighty = 1.0
		c.anchor = GridBagConstraints.NORTH
		layout.setConstraints(basePane, c)
		add(basePane)
	}

	/**
	 * Setup the body which contains the License Text and About text.
	 */
	private fun setupBody() {
		display = JEditorPane()
		display!!.setContentType("text/html")
		display!!.text = about
		display!!.isEditable = false

		if (about == "") {
			display!!.text = "No about text available."
		}

		val buttonPane = JPanel(FlowLayout(FlowLayout.LEADING))
		buttonPane.setBorder(BorderFactory.createEmptyBorder())

		if (license != "" && about != "") {
			val aButton = JRadioButton(I18N.strings.addon_about_about.localized())
			aButton.addItemListener(action)
			aButton.setSelected(true)
			val lButton = JRadioButton(I18N.strings.addon_about_license.localized())

			val btnGrp = ButtonGroup()
			btnGrp.add(aButton)
			btnGrp.add(lButton)

			buttonPane.add(aButton)
			buttonPane.add(lButton)
		} else if (license != "" && about == "") {
			val licenseLabel2 = JLabel(I18N.strings.addon_about_license.localized())
			buttonPane.add(licenseLabel2)
			display!!.setText(license)
		} else if (license == "" && about != "") {
			val aboutLabel = JLabel(I18N.strings.addon_about_about.localized())
			buttonPane.add(aboutLabel)
		}

		val scrollLayout = GridBagLayout()
		val panel = JPanel(scrollLayout)
		val aboutScroll = JScrollPane(display)
		aboutScroll.setBorder(BorderFactory.createEmptyBorder())
		aboutScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER)
		aboutScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED)
		aboutScroll.minimumSize = Dimension(150, 100)
		aboutScroll.getVerticalScrollBar().setBlockIncrement(16)
		aboutScroll.getVerticalScrollBar().setUnitIncrement(16)
		display!!.minimumSize = Dimension(150, 100)

		val c = GridBagConstraints()
		c.fill = GridBagConstraints.BOTH
		c.gridwidth = GridBagConstraints.REMAINDER
		c.gridheight = 2
		c.weightx = 1.0
		c.weighty = 10.0
		c.anchor = GridBagConstraints.NORTH
		layout.setConstraints(panel, c)

		val c2 = GridBagConstraints()
		c2.anchor = GridBagConstraints.WEST
		c2.fill = GridBagConstraints.NONE
		c2.gridwidth = GridBagConstraints.REMAINDER
		c2.gridheight = 1
		c2.weightx = 1.0
		c2.weighty = 1.0
		scrollLayout.setConstraints(buttonPane, c2)
		panel.add(buttonPane)

		val c3 = GridBagConstraints()
		c3.anchor = GridBagConstraints.CENTER
		c3.fill = GridBagConstraints.BOTH
		c3.gridwidth = GridBagConstraints.REMAINDER
		c3.gridheight = 2
		c3.weightx = 1.0
		c3.weighty = 5.0
		scrollLayout.setConstraints(aboutScroll, c3)
		panel.add(aboutScroll)

		add(panel)
	}

	/**
	 * ItemListener implementation which switches the text contained in the JEditorPane.
	 */
	private val action: ItemListener =
		object : ItemListener {
			override fun itemStateChanged(e: ItemEvent) {
				if (e.getStateChange() == ItemEvent.DESELECTED) {
					display!!.text = license
				} else if (e.getStateChange() == ItemEvent.SELECTED) {
					display!!.text = about
				}
				display!!.setCaretPosition(0)
			}
		}

	/**
	 * Creates an new AboutPanel instance.
	 *
	 * @param addonSpecification The add-on manifest content which is the information source.
	 * @param coreAddon Indicates whether the add-on is a core add-on or not.
	 * @param manager
	 * @param dialog
	 */
	init {
		this.setLayout(layout)
		this.dialog = dialog
		this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5))
// 		license = addonSpecification.getLicenseText(LANGUAGE_CODE)
// 		about = addonSpecification.getAbout(LANGUAGE_CODE)
		license = "TODO: License Text"
		about = "TODO: About Text"
		setupHead()
		setupBody()
	}

	companion object {
		private val LANGUAGE_CODE: String = System.getProperty("user.language")
	}
}
