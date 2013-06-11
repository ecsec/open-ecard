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
import javax.swing.JButton;
import java.awt.Component;
import javax.swing.Box;
import javax.swing.JLabel;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import javax.annotation.Nonnull;
import javax.swing.SwingWorker;
import org.openecard.common.interfaces.DispatcherException;
import org.openecard.plugins.PluginAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Entry for the {@link ActionPanel} representing one action.
 * The action is represented as a button and description.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class ActionEntryPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(ActionEntryPanel.class);

    protected final JButton actionBtn;

    /**
     * Creates an entry without the actual action added.
     *
     * @param name Name of the action which is displayed on the button.
     * @param description Name of the description which is displayed besides the button.
     */
    public ActionEntryPanel(@Nonnull String name, @Nonnull String description) {
	setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

	actionBtn = new JButton(name);
	add(actionBtn);

	Component rigidArea = Box.createRigidArea(new Dimension(15, 0));
	add(rigidArea);

	JLabel desc = new JLabel(description);
	desc.setFont(desc.getFont().deriveFont(Font.PLAIN));
	add(desc);
    }

    /**
     * Adds an action from the old plugin mechanism to the entry.
     *
     * @param action Action to perform when the button is pressed.
     * @deprecated Gets removed as soon as the new add-on mechanism is ready.
     */
    @Deprecated
    public void addAction(final PluginAction action) {
	actionBtn.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		new SwingWorker() {
		    @Override
		    protected Object doInBackground() throws Exception {
			actionBtn.setEnabled(false);
			try {
			    action.perform();
			} catch (DispatcherException ex) {
			    logger.error("Failed to dispatch a critical message.", ex);
			} catch (InvocationTargetException ex) {
			    logger.error("Failed to execute plugin successfully.", ex);
			}
			actionBtn.setEnabled(true);
			return null;
		    }
		}.execute();
	    }
	});
    }

}
