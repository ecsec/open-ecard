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

package org.openecard.richclient.gui.manage;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import javax.annotation.Nonnull;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import org.openecard.addon.AddonManager;
import org.openecard.addon.bind.AppExtensionAction;
import org.openecard.addon.manifest.AddonSpecification;
import org.openecard.addon.manifest.AppExtensionSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Entry for the {@link ActionPanel} representing one action.
 * The action is represented as a button and description.
 *
 * @author Tobias Wich
 */
public final class ActionEntryPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final String LANGUAGE_CODE = System.getProperty("user.language");
    private static final Logger LOG = LoggerFactory.getLogger(ActionEntryPanel.class);

    protected final JButton actionBtn;
    protected final AddonManager manager;

    /**
     * Creates an entry without the actual action added.
     *
     * @param addonSpec Id of the addon this action belongs to.
     * @param actionSpec ActionDescription for which this ActionEntryPanel is constructed.
     * @param manager
     */
    public ActionEntryPanel(@Nonnull AddonSpecification addonSpec, @Nonnull AppExtensionSpecification actionSpec,
	    @Nonnull AddonManager manager) {
	setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

	String name = actionSpec.getLocalizedName(LANGUAGE_CODE);
	String description = actionSpec.getLocalizedDescription(LANGUAGE_CODE);

	actionBtn = new JButton(name);
	add(actionBtn);

	Component rigidArea = Box.createRigidArea(new Dimension(15, 0));
	add(rigidArea);

	JLabel desc = new JLabel(description);
	desc.setFont(desc.getFont().deriveFont(Font.PLAIN));
	add(desc);

	this.manager = manager;
	addAction(addonSpec, actionSpec);
    }

    /**
     * Adds an action to the entry.
     *
     * @param action Action to perform when the button is pressed.
     */
    private void addAction(@Nonnull AddonSpecification addonSpec, @Nonnull AppExtensionSpecification actionSpec) {
	actionBtn.addActionListener((ActionEvent e) -> {
	    new SwingWorker<Void, Void>() {
		@Override
		protected Void doInBackground() throws Exception {
		    AppExtensionAction action = manager.getAppExtensionAction(addonSpec, actionSpec.getId());
		    actionBtn.setEnabled(false);
		    try {
			action.execute();
			return null;
		    } catch (Throwable t) {
			// this catch is here just in case anything uncaught is thrown during execute
			LOG.error("Execution ended with an error.", t);
			throw t;
		    } finally {
			manager.returnAppExtensionAction(action);
			actionBtn.setEnabled(true);
		    }
		}
	    }.execute();
	});
    }

}
