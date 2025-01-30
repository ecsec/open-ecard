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

package org.openecard.richclient.gui

import org.openecard.gui.swing.Logo
import java.awt.Dimension
import java.awt.Font
import java.awt.Insets
import javax.swing.*

/**
 * @author Moritz Horsch
 */
class MessageDialog @JvmOverloads constructor(headline: String? = "", message: String? = "") :
    JPanel() {
    private val messageLabel = JTextArea(message)
    private val headlineLabel = JLabel(headline)

    init {
        preferredSize = Dimension(425, 85)
        initComponents()
    }

    private fun initComponents() {
        // Config GUI components
        headlineLabel.font = headlineLabel.font.deriveFont(Font.BOLD)

        messageLabel.margin = Insets(0, 0, 0, 0)
        messageLabel.isEditable = false
        messageLabel.lineWrap = true
        messageLabel.wrapStyleWord = true
        messageLabel.font = JButton().font

        val scrollPane = JScrollPane(messageLabel)
        scrollPane.border = BorderFactory.createEmptyBorder()

        val logo = Logo()

        // Config layout
        val layout = GroupLayout(this)
        setLayout(layout)

        layout.autoCreateGaps = true
        layout.autoCreateContainerGaps = true

        layout.setHorizontalGroup(
            layout.createSequentialGroup()
                .addComponent(logo, 60, 60, 60)
                .addGap(20)
                .addGroup(
                    layout.createParallelGroup()
                        .addComponent(headlineLabel)
                        .addComponent(scrollPane)
                )
        )
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(logo)
                .addGroup(
                    layout.createSequentialGroup()
                        .addComponent(headlineLabel)
                        .addComponent(scrollPane)
                )
        )
    }

    fun setHeadline(headline: String?) {
        headlineLabel.text = headline
    }

    var message: String?
        get() = messageLabel.text
        set(message) {
            messageLabel.text = message
        }

    companion object {
        private const val serialVersionUID = 1L
    }
}
