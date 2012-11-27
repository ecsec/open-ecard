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

package org.openecard.gui.swing;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.openecard.common.util.FileUtils;
import org.openecard.gui.definition.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @editor Florian Feldmann <florian.feldmann@rub.de>
 */
public final class StepBar extends JPanel {

    private static final Logger logger = LoggerFactory.getLogger(StepBar.class);
    private static final long serialVersionUID = 1L;

    private static final ImageIcon loader;

    private List<Step> steps;
    private List<JLabel> labels;
    private int curIdx;

    static {
	URL loaderUrl = FileUtils.resolveResourceAsURL(StepBar.class, "loader.gif");
	loader = new ImageIcon(loaderUrl);
    }


    /**
     * Initialize StepBar for the given steps.
     * The index is initialized to -1.
     *
     * @param steps Steps of the GUI.
     */
    public StepBar(List<Step> steps) {
	this.curIdx = -1;
	updateStepBar(steps);
    }

    /**
     * Update the StepBar to a new set of steps.
     * The index is kept in tact. Usually the list of the steps should only be extended.
     *
     * @param steps New set of steps.
     */
    public void updateStepBar(List<Step> steps) {
	this.steps = steps;
	this.labels = new ArrayList<JLabel>(steps.size());
	removeAll();
	initializeLayout();
	initializeComponents();
    }

    /**
     * Select the step referenced by the given index.
     *
     * @param nextIdx Index of the step which is selected.
     */
    public void selectIdx(final int nextIdx) {
	final int oldIdx = curIdx;
	curIdx = nextIdx;
	logger.debug("Selecting index {}, previous was {}.", nextIdx, oldIdx);

	if (oldIdx >= 0 && oldIdx < getComponentCount()) {
	    // reset last displayed element
	    getComponent(oldIdx).setForeground(Color.GRAY);
	}
	if (nextIdx >= 0 && nextIdx < getComponentCount()) {
	    // Highlight current element
	    getComponent(nextIdx).setForeground(Color.BLACK);
	}
    }

    /**
     * Enable loader icon for the currently highlighted element.
     */
    public void enableLoaderImage() {
	if (curIdx >= 0 && curIdx < labels.size()) {
	    JLabel label = labels.get(curIdx);
	    label.setIcon(loader);
	}
    }

    /**
     * Disable loader icon for the currently highlighted element.
     */
    public void disableLoaderImage() {
	if (curIdx >= 0 && curIdx < labels.size()) {
	    JLabel label = labels.get(curIdx);
	    label.setIcon(null);
	}
    }


    private void initializeLayout() {
	setLayout(new GridBagLayout());
    }

    private void initializeComponents() {
	GridBagConstraints gbc = new GridBagConstraints();
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.anchor = GridBagConstraints.PAGE_START;
	gbc.ipady = 10;
	gbc.gridwidth = GridBagConstraints.REMAINDER;

	for (String names : getStepNames(steps)) {
	    JLabel l = new JLabel(names);
	    labels.add(l);
	    l.setIconTextGap(10);
	    l.setHorizontalTextPosition(JLabel.LEFT);
	    l.setForeground(Color.GRAY);
	    add(l, gbc);
	}

	gbc.weighty = 1.0;
	add(new JLabel(), gbc);
    }

    private static String[] getStepNames(List<Step> steps) {
	ArrayList<String> stepNames = new ArrayList<String>(steps.size());
	for (Step s : steps) {
	    stepNames.add(s.getTitle());
	}
	return stepNames.toArray(new String[steps.size()]);
    }

}
