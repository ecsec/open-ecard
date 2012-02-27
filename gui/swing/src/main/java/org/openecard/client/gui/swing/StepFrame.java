package org.openecard.client.gui.swing;

import org.openecard.client.gui.ResultStatus;
import org.openecard.client.gui.swing.components.StepComponent;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.openecard.client.gui.StepResult;
import org.openecard.client.gui.definition.OutputInfoUnit;
import org.openecard.client.gui.definition.Step;
import org.openecard.client.gui.swing.steplayout.StepLayouter;


/**
 * The StepFrame class represents a single step. The actual layouting is however
 * deferred to a layouting component.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @editor Florian Feldmann <florian.feldmann@rub.de>
 */
public class StepFrame {

    private final Step step;

    private final JPanel rootPanel;
    private final JButton backButton;
    private final JButton forwardButton;
    private final JButton cancelButton;

    private final List<StepComponent> components;

    private SwingStepResult stepResult;
    
    // button descriptors
    private String TEXT_BACK_BUTTON = "Zurück";
    private String TEXT_FINISHED_BUTTON = "Fertig";
    private String TEXT_FORWARD_BUTTON = "Weiter";
    private String TEXT_CANCEL_BUTTON = "Abbrechen";

    public StepFrame(Step step, String dialogType, boolean last) {
	this.step = step;
	// create panels
	BorderLayout layout = new BorderLayout();
	rootPanel = new JPanel(layout);
	FlowLayout buttonLayout = new FlowLayout();
	JPanel buttonPanel = new JPanel(buttonLayout);
	rootPanel.add(buttonPanel, BorderLayout.SOUTH);

	// create button elements
	backButton = new JButton();
	forwardButton = new JButton();
	cancelButton = new JButton();
	buttonPanel.add(backButton);
	buttonPanel.add(forwardButton);
	buttonPanel.add(cancelButton);
	// back
	backButton.setText(TEXT_BACK_BUTTON);
	if (! step.isReversible()) {
	    backButton.setEnabled(false);
	} else {
	    backButton.addActionListener(new BackEvent());
	}
	// forward
	if (last) {
	    forwardButton.setText(TEXT_FINISHED_BUTTON);
	    forwardButton.addActionListener(new ForwardEvent());
	} else {
	    forwardButton.setText(TEXT_FORWARD_BUTTON);
	    forwardButton.addActionListener(new ForwardEvent());
	}
	// cancel
	cancelButton.removeAll();
	cancelButton.setText(TEXT_CANCEL_BUTTON);
	cancelButton.addActionListener(new CancelEvent());

        // dummy JLabel for alignment, adjusts free space left of content
        JLabel dummy = new JLabel("   ");
        rootPanel.add(dummy, BorderLayout.WEST);

	// fill content panel - this is done with an external class which knows all about the actual layout
	StepLayouter stepLayouter = StepLayouter.create(step.getInputInfoUnits(), dialogType, step.getName());
	Container contentPanel = stepLayouter.getPanel();
	rootPanel.add(contentPanel, BorderLayout.CENTER);
	components = stepLayouter.getComponents();
    }

    public void resetForDisplay() {
	stepResult = new SwingStepResult();
    }

    public void instantReturnIfSet() {
	if (step.isInstantReturn()) {
	    forwardButton.doClick();
	}
    }

    public Container getPanel() {
	return rootPanel;
    }

    /**
     * Check if all components on the frame are valid. This can be used to see
     * if a jump to the next frame can be made.
     * @return True if all components are valid, false otherwise.
     */
    public boolean validate() {
	for (StepComponent next : components) {
	    if (next.isValueType() && ! next.validate()) {
		return false;
	    }
	}
	return true;
    }

    /**
     * Get result for all components on the frame that support result values.
     * @return List containg all result values. As a matter of fact this list can be empty.
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


    public StepResult getStepResult() {
	return stepResult;
    }


    /**
     * Event class for cancel button clicks.
     */
    private class CancelEvent implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent e) {
	    synchronized (stepResult) {
		stepResult.cancelled = true;
		stepResult.done = true;
	    }
	    try {
		stepResult.syncPoint.exchange(null, 500, TimeUnit.MILLISECONDS);
	    } catch (InterruptedException ex) {
	    } catch (TimeoutException ex) {
	    }
	}
    }
    /**
     * Event class for forward button clicks.
     */
    private class ForwardEvent implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent e) {
	    if (validate()) {
		synchronized (stepResult) {
		    stepResult.done = true;
		    stepResult.results = getResultContent();
		}
		try {
		    stepResult.syncPoint.exchange(null, 500, TimeUnit.MILLISECONDS);
		} catch (InterruptedException ex) {
		} catch (TimeoutException ex) {
		}
	    }
	}
    }
    /**
     * Event class for back button clicks.
     */
    private class BackEvent implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent e) {
	    synchronized (stepResult) {
		stepResult.back = true;
		stepResult.done = true;
	    }
	    try {
		stepResult.syncPoint.exchange(null, 500, TimeUnit.MILLISECONDS);
	    } catch (InterruptedException ex) {
	    } catch (TimeoutException ex) {
	    }
	}
    }



    private class SwingStepResult implements StepResult {

	public Exchanger syncPoint = new Exchanger();
	public boolean done = false;
	public boolean cancelled = false;
	public boolean back = false;
	public List<OutputInfoUnit> results = null;

	@Override
	public String stepName() {
	    return step.getName();
	}

	@Override
	public ResultStatus status() {
	    if (!done) {
		try {
		    syncPoint.exchange(null);
		} catch (InterruptedException ex) {
		}
	    }
	    // return appropriate result
	    synchronized (this) {
		if (cancelled) {
		    return ResultStatus.CANCEL;
		} else if (back) {
		    return ResultStatus.BACK;
		} else {
		    return ResultStatus.OK;
		}
	    }
	}

	@Override
	public boolean isOK() {
	    if (!done) {
		try {
		    syncPoint.exchange(null);
		} catch (InterruptedException ex) {
		}
	    }
	    synchronized (this) {
		return status() == ResultStatus.OK;
	    }
	}

	@Override
	public boolean isBack() {
	    if (!done) {
		try {
		    syncPoint.exchange(null);
		} catch (InterruptedException ex) {
		}
	    }
	    synchronized (this) {
		return status() == ResultStatus.BACK;
	    }
	}

	@Override
	public boolean isCancelled() {
	    if (!done) {
		try {
		    syncPoint.exchange(null);
		} catch (InterruptedException ex) {
		}
	    }
	    synchronized (this) {
		return status() == ResultStatus.CANCEL;
	    }
	}

	@Override
	public List<OutputInfoUnit> results() {
	    if (!done) {
		try {
		    syncPoint.exchange(null);
		} catch (InterruptedException ex) {
		}
	    }
	    synchronized (this) {
		if (results == null) {
		    results = Collections.unmodifiableList(new LinkedList());
		}
		return results;
	    }
	}

    }

}
