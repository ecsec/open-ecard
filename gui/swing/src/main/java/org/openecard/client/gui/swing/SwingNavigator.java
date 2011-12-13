package org.openecard.client.gui.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import org.openecard.client.gui.StepResult;
import org.openecard.client.gui.UserConsentNavigator;
import org.openecard.client.gui.definition.Step;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class SwingNavigator implements UserConsentNavigator {

    private final Container stepContainer;
    private final DialogWrapper dialogWrapper;

    private final String dialogType;
    private final Sidebar sidebar;
    private final ArrayList<StepFrame> stepFrames;
    private final int numSteps;

    private int curStep = -1;

    public SwingNavigator(DialogWrapper dialogWrapper, String dialogType, List<Step> steps, Container stepContainer, Sidebar sidebar) {
	this.dialogWrapper = dialogWrapper;
	this.stepContainer = stepContainer;
	this.dialogType = dialogType;
	this.sidebar = sidebar;
	this.numSteps = steps.size(); // separate field, otherwise selectIdx fails on first invocation

	this.stepFrames = createStepFrames(steps);
	this.dialogWrapper.showDialog();
    }


    @Override
    public boolean hasNext() {
	return curStep < numSteps-1;
    }

    @Override
    public StepResult current() {
	selectIdx(curStep);
	return stepFrames.get(curStep).getStepResult();
    }

    @Override
    public StepResult next() {
	// TODO: add safeguards
	selectIdx(curStep+1);
	return stepFrames.get(curStep).getStepResult();
    }

    @Override
    public StepResult previous() {
	// TODO: add safeguards
	selectIdx(curStep-1);
	return stepFrames.get(curStep).getStepResult();
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
	dialogWrapper.hideDialog();
    }


    private ArrayList<StepFrame> createStepFrames(List<Step> steps) {
	ArrayList frames = new ArrayList(steps.size());
	for (int i=0; i < steps.size(); i++) {
	    if (i == 0) {
		steps.get(0).setReversible(false);
	    }
	    Step s = steps.get(i);
	    frames.add(createStep(s, (i==steps.size()-1) ? true : false));
	}
	return frames;
    }

    private StepFrame createStep(Step step, boolean last) {
	StepFrame sf = new StepFrame(step, dialogType, last);

	return sf;
    }

    private void selectIdx(int idx) {
	// sidebar
	sidebar.selectIdx(idx);
	// content replacement
	stepContainer.removeAll();
	BorderLayout layout = new BorderLayout();
	stepContainer.setLayout(layout);
	StepFrame nextStep = stepFrames.get(idx);
	Container nextPanel = nextStep.getPanel();
	stepContainer.add(nextPanel, BorderLayout.CENTER);
	curStep = idx;
	nextStep.resetForDisplay();
	stepContainer.validate();
	stepContainer.repaint();
	nextStep.instantReturnIfSet();
    }

}
