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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.openecard.gui.ResultStatus;
import org.openecard.gui.StepResult;
import org.openecard.gui.definition.OutputInfoUnit;
import org.openecard.gui.definition.Step;
import org.openecard.gui.swing.common.NavigationEvent;
import org.openecard.gui.swing.components.StepComponent;
import org.openecard.gui.swing.steplayout.StepLayouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The StepFrame class represents a single step.
 * The actual layouting is however deferred to a layouting component.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @author Florian Feldmann <florian.feldmann@rub.de>
 */
public class StepFrame {

    private static final Logger logger = LoggerFactory.getLogger(StepFrame.class);
    private static final long serialVersionUID = 1L;

    private final JPanel panel;
    private final Step step;
    private final String dialogType;
    private SwingStepResult stepResult;
    private List<StepComponent> components;

    public StepFrame(Step step, String dialogType) {
	this.panel = new JPanel();
	this.step = step;
	this.dialogType = dialogType;
	this.stepResult = new SwingStepResult(step);

	initLayout();
    }

    private void initLayout() {
	panel.setLayout(new BorderLayout());
    }

    private void initComponents() {
	StepLayouter stepLayouter = StepLayouter.create(step.getInputInfoUnits(), dialogType, step.getTitle());
	Container contentPanel = stepLayouter.getPanel();
	panel.add(contentPanel, BorderLayout.CENTER);

	components = stepLayouter.getComponents();
    }

    public void resetResult() {
	stepResult = new SwingStepResult(step);
    }

    public boolean isInstantReturn() {
	return step.isInstantReturn();
    }

    public Container getPanel() {
	revalidate(panel);
	return panel;
    }

    public Step getStep() {
	return step;
    }

    /**
     * Check if all components on the frame are valid. This can be used to see
     * if a jump to the next frame can be made.
     *
     * @return True if all components are valid, false otherwise.
     */
    public boolean validateComponents() {
	for (StepComponent next : components) {
	    Component component = next.getComponent();
	    if (next.isValueType() && ! next.validate()) {
		component.setBackground(Color.RED);
		return false;
	    }
	    component.setBackground(null);
	}
	return true;
    }

    /**
     * Get result for all components on the frame that support result values.
     *
     * @return List containing all result values. As a matter of fact this list can be empty.
     */
    public List<OutputInfoUnit> getResultContent() {
	ArrayList<OutputInfoUnit> result = new ArrayList<OutputInfoUnit>(components.size());
	for (StepComponent next : components) {
	    if (next.isValueType()) {
		result.add(next.getValue());
	    }
	}
	return result;
    }

    public void updateFrame() {
	panel.removeAll();
	initComponents();
	revalidate(panel);
    }

    public StepResult getStepResult() {
	return stepResult;
    }


    private void revalidate(JComponent c) {
	for (int i = 0; i < c.getComponentCount(); i++) {
	    this.revalidate((JComponent) c.getComponent(i));
	}
	c.revalidate();
	c.repaint();
    }


    /**
     * Locks elements on the frame, so they can not be modified anymore.
     * This is needed when executing an action. That is the time between a button click and the update of the frame
     * panel.
     */
    public void lockControls() {
	// TODO: lock all elements
    }
    /**
     * Unlocks elements on this frame, so that they can be modified.
     * This is needed to unlock the frame when it is displayed after it has been locked by an action.
     */
    public void unlockControls() {
	// TODO: unlock elements of this frame
    }

    /**
     * Updates the StepResult when a button is clicked.
     * Before a button is clicked by the user, the {@link org.openecard.gui.executor.ExecutionEngine} waits for the
     * result content by calling {@link StepResult#getStatus()}. This method sets the portions of the result relevant
     * for the respective button event and unlocks the getStatus method.
     *
     * @param event Event describing which button has been clicked.
     */
    public void updateResult(NavigationEvent event) {
	if (event == NavigationEvent.BACK) {
	    stepResult.setResult(getResultContent());
	    stepResult.setResultStatus(ResultStatus.BACK);
	} else if (event == NavigationEvent.NEXT) {
	    stepResult.setResult(getResultContent());
	    stepResult.setResultStatus(ResultStatus.OK);
	} else if (event == NavigationEvent.CANCEL) {
	    stepResult.setResultStatus(ResultStatus.CANCEL);
	}

	try {
	    logger.debug("Exchange result for step '{}", step.getTitle());
	    stepResult.syncPoint.exchange(null);
	} catch (Exception ignore) {
	}
    }

}
