package org.openecard.client.gui.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import org.openecard.client.gui.StepResult;
import org.openecard.client.gui.UserConsentNavigator;
import org.openecard.client.gui.definition.Step;

/**
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class SwingNavigator implements UserConsentNavigator {

    public static String PROPERTY_CURRENT_STEP;
    private final Container stepContainer;
    private final DialogWrapper dialogWrapper;
    private String dialogType;
    private ArrayList<StepFrame> stepFrames;
    private int numSteps;
    private int curStep = -1;
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private Navigation navi;

    public SwingNavigator(DialogWrapper dialogWrapper, String dialogType, List<Step> steps, Container stepContainer, Navigation navi) {
	this.navi = navi;
	this.dialogWrapper = dialogWrapper;
	this.stepContainer = stepContainer;
	this.dialogType = dialogType;
	this.numSteps = steps.size();

	this.stepFrames = createStepFrames(steps);
	this.dialogWrapper.showDialog();
    }

    @Override
    public boolean hasNext() {
	return curStep < numSteps - 1;
    }

    @Override
    public StepResult current() {
	selectIdx(curStep);
	return stepFrames.get(curStep).getStepResult();
    }

    @Override
    public StepResult next() {
	// TODO: add safeguards
	selectIdx(curStep + 1);
	firePropertyChange(PROPERTY_CURRENT_STEP, curStep, curStep - 1);
	return stepFrames.get(curStep).getStepResult();
    }

    @Override
    public StepResult previous() {
	// TODO: add safeguards
	selectIdx(curStep - 1);
	firePropertyChange(PROPERTY_CURRENT_STEP, curStep, curStep + 1);
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
	for (int i = 0; i < steps.size(); i++) {
	    if (i == 0) {
		steps.get(0).setReversible(false);
	    }
	    Step s = steps.get(i);
	    frames.add(createStep(s));
	}
	return frames;
    }

    private StepFrame createStep(Step step) {
	StepFrame sf = new StepFrame(step, dialogType);
	navi.addActionListener(sf);

	return sf;
    }

    private void selectIdx(int idx) {
	// content replacement
	stepContainer.removeAll();
	BorderLayout layout = new BorderLayout();
	stepContainer.setLayout(layout);
	StepFrame nextStep = stepFrames.get(idx);
	Container nextPanel = nextStep.getPanel();
	stepContainer.add(nextPanel, BorderLayout.CENTER);
	stepContainer.add(nextPanel);
	curStep = idx;
	nextStep.resetForDisplay();
	stepContainer.validate();
	stepContainer.repaint();
	nextStep.instantReturnIfSet();
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
