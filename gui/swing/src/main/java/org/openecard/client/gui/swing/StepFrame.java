package org.openecard.client.gui.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.openecard.client.gui.ResultStatus;
import org.openecard.client.gui.StepResult;
import org.openecard.client.gui.definition.OutputInfoUnit;
import org.openecard.client.gui.definition.Step;
import org.openecard.client.gui.swing.components.StepComponent;
import org.openecard.client.gui.swing.steplayout.StepLayouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The StepFrame class represents a single step. The actual layouting is however
 * deferred to a layouting component.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @editor Florian Feldmann <florian.feldmann@rub.de>
 */
public class StepFrame extends JPanel implements ActionListener {

    private static final Logger logger = LoggerFactory.getLogger(StepFrame.class);
    private Step step;
    private String dialogType;
    private List<StepComponent> components;
    private SwingStepResult stepResult;

    public StepFrame(Step step, String dialogType) {
	this.step = step;
	this.dialogType = dialogType;

	stepResult = new SwingStepResult(step.getID());

	initLayout();
    }

    private void initLayout() {
	setLayout(new BorderLayout());
    }

    private void initComponents() {
	StepLayouter stepLayouter = StepLayouter.create(step.getInputInfoUnits(), dialogType, step.getTitle());
	Container contentPanel = stepLayouter.getPanel();
	add(contentPanel, BorderLayout.CENTER);

	components = stepLayouter.getComponents();
    }

    public void resetForDisplay() {
	stepResult = new SwingStepResult(step.getID());
    }

    public void instantReturnIfSet() {
	if (step.isInstantReturn()) {
	    //TODO
//	    forwardButton.doClick();
	}
    }

    //OK
    public Container getPanel() {
	revalidate(this);
	return this;
    }

    /**
     * Check if all components on the frame are valid. This can be used to see
     * if a jump to the next frame can be made.
     *
     * @return True if all components are valid, false otherwise.
     */
    public boolean validateComponents() {
	for (StepComponent next : components) {
	    if (next.isValueType() && !next.validate()) {
		return false;
	    }
	}
	return true;
    }

    /**
     * Get result for all components on the frame that support result values.
     *
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
	removeAll();
	initComponents();
	revalidate(this);
	return stepResult;
    }

    private void revalidate(JComponent c) {
	for (int i = 0; i < c.getComponentCount(); i++) {
	    this.revalidate((JComponent) c.getComponent(i));
	}
	c.revalidate();
	c.repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	logger.info("StepFrame event: {}", e.paramString());

	if (e.getSource().equals(step)) {
	    String command = e.getActionCommand();
	    if (command.equals(GUIConstants.BUTTON_BACK)) {
		stepResult.setResult(getResultContent());
		stepResult.setResultStatus(ResultStatus.BACK);
	    } else if (command.equals(GUIConstants.BUTTON_NEXT)) {
		stepResult.setResult(getResultContent());
		stepResult.setResultStatus(ResultStatus.OK);
	    } else if (command.equals(GUIConstants.BUTTON_CANCEL)) {
		stepResult.setResultStatus(ResultStatus.CANCEL);
	    } else {
		return;
	    }
	    try {
		stepResult.syncPoint.exchange(null);
	    } catch (Exception ignore) {
	    }
	}
    }
}
