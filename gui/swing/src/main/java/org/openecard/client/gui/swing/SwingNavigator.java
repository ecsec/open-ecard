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

package org.openecard.client.gui.swing;

import java.awt.Container;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import org.openecard.client.gui.ResultStatus;
import org.openecard.client.gui.StepResult;
import org.openecard.client.gui.UserConsentNavigator;
import org.openecard.client.gui.definition.Step;


/**
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class SwingNavigator implements UserConsentNavigator {

    public static String PROPERTY_CURRENT_STEP = "PROPERTY_CURRENT_STEP";
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private Container stepContainer;
    private DialogWrapper dialogWrapper;
    private ArrayList<StepFrame> stepFrames;
    private int stepPointer = -1;

    public SwingNavigator(DialogWrapper dialogWrapper, String dialogType, List<Step> steps, Container stepContainer) {
	this.dialogWrapper = dialogWrapper;
	this.stepContainer = stepContainer;
	this.stepFrames = createStepFrames(steps, dialogType);

	this.dialogWrapper.show();
    }

    @Override
    public boolean hasNext() {
	return stepPointer < (stepFrames.size() - 1);
    }

    @Override
    public StepResult current() {
	selectIdx(stepPointer);
	return stepFrames.get(stepPointer).getStepResult();
    }

    @Override
    public StepResult next() {
	if (hasNext()) {
	    selectIdx(stepPointer + 1);
	    firePropertyChange(PROPERTY_CURRENT_STEP, stepPointer, stepPointer - 1);
	    return stepFrames.get(stepPointer).getStepResult();
	}
	return new SwingStepResult(null, ResultStatus.CANCEL);
    }

    @Override
    public StepResult previous() {
	if (stepPointer > 0) {
	    selectIdx(stepPointer - 1);
	    firePropertyChange(PROPERTY_CURRENT_STEP, stepPointer, stepPointer + 1);
	    return stepFrames.get(stepPointer).getStepResult();
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

    public List<StepFrame> getStepFrames() {
	return stepFrames;
    }

    private ArrayList<StepFrame> createStepFrames(List<Step> steps, String dialogType) {
	ArrayList frames = new ArrayList(steps.size());
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
	Container nextPanel = nextStep.getPanel();
	nextStep.resetResult();

	stepContainer.removeAll();
	stepContainer.add(nextPanel);
	stepContainer.validate();
	stepContainer.repaint();

	stepPointer = idx;
	//TODO
//	nextStep.instantReturnIfSet();
    }

    public void addPropertyChangeListener(PropertyChangeListener p) {
	propertyChangeSupport.addPropertyChangeListener(p);
    }

    public void removePropertyChangeListener(PropertyChangeListener p) {
	propertyChangeSupport.removePropertyChangeListener(p);
    }

    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
	propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }
}
