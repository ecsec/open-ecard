/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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
package org.openecard.gui.swing

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.common.util.FileUtils.resolveResourceAsURL
import org.openecard.gui.definition.Step
import java.awt.Color
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.UIManager

private val LOG = KotlinLogging.logger { }

/**
 * @author Moritz Horsch
 * @author Tobias Wich
 * @author Florian Feldmann
 */
class StepBar(
	steps: MutableList<Step>,
) : JPanel() {
	private var steps: MutableList<Step>
	private var labels: MutableList<JLabel>
	private var curIdx: Int

	/**
	 * Initialize StepBar for the given steps.
	 * The index is initialized to -1.
	 *
	 * @param steps Steps of the GUI.
	 */
	init {
		this.curIdx = -1
		this.steps = steps
		this.labels = mutableListOf()
		initializeLayout()
		updateStepBar(steps)
	}

	/**
	 * Update the StepBar to a new set of steps.
	 * The index is kept in tact. Usually the list of the steps should only be extended.
	 *
	 * @param steps New set of steps.
	 */
	fun updateStepBar(steps: MutableList<Step>) {
		this.steps = steps
		this.labels = ArrayList<JLabel>(steps.size)
		removeAll()
		initializeComponents()
	}

	/**
	 * Select the step referenced by the given index.
	 *
	 * @param nextIdx Index of the step which is selected.
	 */
	fun selectIdx(nextIdx: Int) {
		val oldIdx = curIdx
		curIdx = nextIdx
		LOG.debug { "Selecting index $nextIdx, previous was $oldIdx." }

		if (oldIdx >= 0 && oldIdx < componentCount) {
			// reset last displayed element
			getComponent(oldIdx).setForeground(Color.GRAY)
		}
		if (nextIdx >= 0 && nextIdx < componentCount) {
			// Highlight current element
			getComponent(nextIdx).setForeground(Color.BLACK)
		}
	}

	/**
	 * Enable loader icon for the currently highlighted element.
	 */
	fun enableLoaderImage() {
		if (curIdx >= 0 && curIdx < labels.size) {
			val label = labels[curIdx]
			label.setIcon(LOADER)
		}
	}

	/**
	 * Disable loader icon for the currently highlighted element.
	 */
	fun disableLoaderImage() {
		if (curIdx >= 0 && curIdx < labels.size) {
			val label = labels[curIdx]
			label.setIcon(null)
		}
	}

	private fun initializeLayout() {
		setLayout(GridBagLayout())
	}

	private fun initializeComponents() {
		val gbc = GridBagConstraints()
		gbc.fill = GridBagConstraints.HORIZONTAL
		gbc.anchor = GridBagConstraints.PAGE_START
		gbc.ipady = 10
		gbc.gridwidth = GridBagConstraints.REMAINDER
		gbc.weightx = 1.0

		val font = UIManager.getFont("Label.font").deriveFont(Font.PLAIN)
		for (s in steps) {
			val name = s.title
			val l = JLabel(name)
			l.setFont(font)
			labels.add(l)
			l.setIconTextGap(10)
			l.setHorizontalTextPosition(JLabel.LEFT)
			l.setForeground(Color.GRAY)
			add(l, gbc)
		}

		gbc.weighty = 1.0
		add(JLabel(), gbc)
	}

	companion object {
		private const val serialVersionUID = 1L

		private val LOADER: ImageIcon

		init {
			val loaderUrl = resolveResourceAsURL(StepBar::class.java, "loader.gif")
			LOADER = ImageIcon(loaderUrl)
		}
	}
}
