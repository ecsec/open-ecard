/****************************************************************************
 * Copyright (C) 2013 ecsec GmbH.
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

package org.openecard.richclient.gui.manage;

import javax.swing.JPanel;
import javax.swing.BoxLayout;
import java.awt.Component;
import javax.swing.Box;
import java.awt.Dimension;
import javax.annotation.Nonnull;
import javax.swing.BorderFactory;


/**
 * Panel aggregating several action entries.
 * The entries are of type {@link ActionEntryPanel}.
 *
 * @author Tobias Wich
 */
public class ActionPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final Component glue;

    /**
     * Creates a panel instance.
     */
    public ActionPanel() {
	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0));

	Component space = Box.createRigidArea(new Dimension(20, 10));
	add(space);

	glue = Box.createGlue();
	add(glue);
    }

    /**
     * Adds an action entry to this panel.
     *
     * @param actionEntry Entry to add to the panel.
     */
    protected void addActionEntry(@Nonnull ActionEntryPanel actionEntry) {
	remove(glue);

	actionEntry.setAlignmentY(Component.TOP_ALIGNMENT);
	actionEntry.setAlignmentX(Component.LEFT_ALIGNMENT);
	add(actionEntry);

	Component rigidArea = Box.createRigidArea(new Dimension(20, 20));
	add(rigidArea);

	add(glue);
    }

}
