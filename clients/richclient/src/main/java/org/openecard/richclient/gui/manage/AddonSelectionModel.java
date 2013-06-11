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

import java.awt.BorderLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashMap;
import javax.annotation.Nonnull;
import javax.swing.AbstractListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


/**
 * Data model for the add-on selection lists on the ManagementDialog.
 * The data model manages the entries as well as the functionality to replace logo and addon page on the dialog.
 * The class implements various listeners, so that the model can be used as listener in the list and dialog.
 * The events processed are {@link #valueChanged(javax.swing.event.ListSelectionEvent)} and
 * {@link #windowClosed(java.awt.event.WindowEvent)}. The listener then saves the currently open settings page and
 * changes the displayed add-on panel if applicable.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class AddonSelectionModel extends AbstractListModel implements ListSelectionListener, WindowListener {

    private static final long serialVersionUID = 1L;

    private int idxCounter;

    private final ManagementDialog dialog;
    private final JPanel container;
    private final HashMap<Integer, String> names;
    private final HashMap<Integer, AddonPanel> addons;
    private final HashMap<Integer, Class<AddonPanel>> addonClasses;

    private AddonPanel lastActivePanel;

    /**
     * Creates a model instance.
     * The model is initialized with the ManagementDialog, so that the logo can be replaced and with the container where
     * the addons will be placed.
     *
     * @param dialog The current ManagementDialog instance.
     * @param addonContainer The panel where the AddonPanels will be put into.
     */
    public AddonSelectionModel(@Nonnull ManagementDialog dialog, @Nonnull JPanel addonContainer) {
	this.dialog = dialog;
	this.container = addonContainer;
	this.names = new HashMap<Integer, String>();
	this.addons = new HashMap<Integer, AddonPanel>();
	this.addonClasses = new HashMap<Integer, Class<AddonPanel>>();
    }

    /**
     * Adds an add-on element to the model.
     *
     * @param name Name displayed in the list.
     * @param addonPanel Panel displayed, when the item is selected.
     */
    public synchronized void addElement(@Nonnull String name, @Nonnull AddonPanel addonPanel) {
	if (! names.containsValue(name)) {
	    names.put(idxCounter, name);
	    addons.put(idxCounter, addonPanel);
	    idxCounter++;
	}
    }

    /**
     * Adds an add-on element to the model.
     * <p><b>The method is not implemented yet!</b></p>
     *
     * @param name Name displayed in the list.
     * @param addonPanel Class of the AddonPanel, so that the panel can be instatiated later.
     * @see #addElement(java.lang.String, org.openecard.richclient.gui.manage.AddonPanel)
     */
    public synchronized void addElement(@Nonnull String name, @Nonnull Class<AddonPanel> addonPanel) {
	throw new UnsupportedOperationException("Not implemented yet.");
    }


    @Override
    public int getSize() {
	return names.size();
    }

    @Override
    public String getElementAt(int index) {
	return names.get(index);
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
	Object source = e.getSource();
	if (! e.getValueIsAdjusting() && source instanceof JList) {
	    JList sourceList = (JList) source;
	    // save last displayed component
	    saveLastDialog();
	    // load other panel if an index is selected
	    int idx = sourceList.getSelectedIndex();
	    if (idx >= 0) {
		AddonPanel panel = getPanel(idx);
		lastActivePanel = panel;
		container.removeAll();
		container.add(panel, BorderLayout.CENTER);
		// invalidate component, else it won't be redrawn
		container.invalidate();
		container.validate();
		container.repaint();
		// update icon in management panel
		dialog.setLogo(panel.getLogo());
	    }
	}
    }

    private synchronized AddonPanel getPanel(int idx) {
	// TODO: load panel from class
	return addons.get(idx);
    }

    private void saveLastDialog() {
	if (lastActivePanel != null) {
	    lastActivePanel.saveProperties();
	    lastActivePanel = null;
	}
    }


    @Override
    public void windowOpened(WindowEvent e) {
	// ignore
    }

    @Override
    public void windowClosing(WindowEvent e) {
	// ignore
    }

    @Override
    public void windowClosed(WindowEvent e) {
	saveLastDialog();
    }

    @Override
    public void windowIconified(WindowEvent e) {
	// ignore
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
	// ignore
    }

    @Override
    public void windowActivated(WindowEvent e) {
	// ignore
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
	// ignore
    }

}
