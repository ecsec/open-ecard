/****************************************************************************
 * Copyright (C) 2013-2018 ecsec GmbH.
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
import org.openecard.addon.AddonManager
import org.openecard.addon.bind.AppExtensionAction
import org.openecard.addon.manifest.AddonSpecification
import org.openecard.addon.manifest.AppExtensionSpecification
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingWorker

private val LOG = KotlinLogging.logger { }

/**
 * Entry for the [ActionPanel] representing one action.
 * The action is represented as a button and description.
 *
 * @author Tobias Wich
 */
class ActionEntryPanel(
	addonSpec: AddonSpecification,
	actionSpec: AppExtensionSpecification,
	manager: AddonManager,
) : JPanel() {
	protected val actionBtn: JButton
	protected val manager: AddonManager

	/**
	 * Creates an entry without the actual action added.
	 *
	 * @param addonSpec Id of the addon this action belongs to.
	 * @param actionSpec ActionDescription for which this ActionEntryPanel is constructed.
	 * @param manager
	 */
	init {
		setLayout(BoxLayout(this, BoxLayout.X_AXIS))

		val name: String = actionSpec.getLocalizedName(LANGUAGE_CODE)
		val description: String = actionSpec.getLocalizedDescription(LANGUAGE_CODE)

		actionBtn = JButton(name)
		add(actionBtn)

		val rigidArea: Component = Box.createRigidArea(Dimension(15, 0))
		add(rigidArea)

		val desc = JLabel(description)
		desc.setFont(desc.getFont().deriveFont(Font.PLAIN))
		add(desc)

		this.manager = manager
		addAction(addonSpec, actionSpec)
	}

	/**
	 * Adds an action to the entry.
	 *
	 * @param actionSpec Action to perform when the button is pressed.
	 */
	private fun addAction(
		addonSpec: AddonSpecification,
		actionSpec: AppExtensionSpecification,
	) {
		actionBtn.addActionListener {
			object : SwingWorker<Unit, Void?>() {
				override fun doInBackground() {
					val action: AppExtensionAction = manager.getAppExtensionAction(addonSpec, actionSpec.id!!)!!
					actionBtn.setEnabled(false)
					try {
						action.execute()
					} catch (t: Throwable) {
						// this catch is here just in case anything uncaught is thrown during execute
						LOG.error(t) { "Execution ended with an error." }
						throw t
					} finally {
						manager.returnAppExtensionAction(action)
						actionBtn.setEnabled(true)
					}
				}
			}.execute()
		}
	}

	companion object {
		private const val serialVersionUID: Long = 1L
		private val LANGUAGE_CODE: String = System.getProperty("user.language")
	}
}
