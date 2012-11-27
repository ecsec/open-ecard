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

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import org.openecard.gui.ResultStatus;
import org.openecard.gui.StepResult;
import org.openecard.gui.UserConsentNavigator;
import org.openecard.gui.definition.Step;
import org.openecard.gui.swing.common.GUIConstants;
import org.openecard.gui.swing.common.NavigationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of the UserConsentNavigator interface for the Swing GUI.
 * This class receives button clicks and orchestrates the update of the steps and progress indication components.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class SwingNavigator implements UserConsentNavigator, ActionListener {

    public static final Logger logger = LoggerFactory.getLogger(SwingNavigator.class);

    private final DialogWrapper dialogWrapper;
    private final Container stepContainer;

    private final ArrayList<StepFrame> stepFrames;
    private final NavigationBar navBar;
    private final StepBar stepBar;

    private int stepPointer = -1;


    public SwingNavigator(DialogWrapper dialogWrapper, String dialogType, List<Step> steps, Container stepContainer,
	    NavigationBar navPanel, StepBar stepBar) {
	this.dialogWrapper = dialogWrapper;
	this.stepContainer = stepContainer;
	this.stepFrames = createStepFrames(steps, dialogType);
	this.navBar = navPanel;
	this.stepBar = stepBar;

	this.dialogWrapper.show();
    }

    @Override
    public boolean hasNext() {
	return stepPointer < (stepFrames.size() - 1);
    }

    @Override
    public StepResult current() {
	stepBar.disableLoaderImage();
	selectIdx(stepPointer);
	StepFrame frame = stepFrames.get(stepPointer);

	// click next button without giving the user the possibility to interfere
	clickIfInstantReturn(frame);

	return frame.getStepResult();
    }

    @Override
    public StepResult next() {
	stepBar.disableLoaderImage();
	if (hasNext()) {
	    selectIdx(stepPointer + 1);
	    StepFrame frame = stepFrames.get(stepPointer);

	    // click next button without giving the user the possibility to interfere
	    clickIfInstantReturn(frame);

	    return frame.getStepResult();
	}
	return new SwingStepResult(null, ResultStatus.CANCEL);
    }

    @Override
    public StepResult previous() {
	stepBar.disableLoaderImage();
	if (stepPointer > 0) {
	    selectIdx(stepPointer - 1);
	    StepFrame frame = stepFrames.get(stepPointer);

	    // click next button without giving the user the possibility to interfere
	    clickIfInstantReturn(frame);

	    return frame.getStepResult();
	}
	return new SwingStepResult(null, ResultStatus.CANCEL);
    }

    @Override
    public StepResult replaceCurrent(Step step) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public StepResult replaceNext(Step step) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public StepResult replacePrevious(Step step) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void close() {
	dialogWrapper.hide();
    }


    private ArrayList<StepFrame> createStepFrames(List<Step> steps, String dialogType) {
	ArrayList<StepFrame> frames = new ArrayList<StepFrame>(steps.size());
	for (int i = 0; i < steps.size(); i++) {
	    if (i == 0) {
		steps.get(0).setReversible(false);
	    }
	    Step s = steps.get(i);
	    StepFrame sf = new StepFrame(s, dialogType);
	    frames.add(sf);
	}
	return frames;
    }


    private void selectIdx(int idx) {
	// Content replacement
	StepFrame nextStep = stepFrames.get(idx);
	stepBar.selectIdx(idx);
	navBar.selectIdx(idx);
	Container nextPanel = nextStep.getPanel();
	nextStep.resetResult();

	stepContainer.removeAll();
	stepContainer.add(nextPanel);
	stepContainer.validate();
	stepContainer.repaint();

	stepPointer = idx;

	nextStep.updateFrame();
	nextStep.unlockControls();
	navBar.unlockControls();
    }


    private void clickIfInstantReturn(StepFrame frame) {
	if (frame.isInstantReturn()) {
	    String command = GUIConstants.BUTTON_NEXT;
	    final ActionEvent e = new ActionEvent(frame.getStep(), ActionEvent.ACTION_PERFORMED, command);
	    // create async invocation of the action
	    new Thread("Instant-Return-Thread") {
		@Override
		public void run() {
		    actionPerformed(e);
		}
	    }.start();
	}
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	logger.debug("Received event: {}", e.getActionCommand());

	NavigationEvent event = NavigationEvent.fromEvent(e);
	if (event == null) {
	    logger.error("Unknown event received: {}", e.getActionCommand());
	    return;
	}

	// lock controls and update current step result
	StepFrame curStep = stepFrames.get(stepPointer);
	stepBar.enableLoaderImage();
	navBar.lockControls();
	curStep.lockControls();
	curStep.updateResult(event);
    }

}
