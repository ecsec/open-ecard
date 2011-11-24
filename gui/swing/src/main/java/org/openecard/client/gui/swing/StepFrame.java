package org.openecard.client.gui.swing;

import org.openecard.client.gui.swing.components.StepComponent;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.openecard.client.gui.swing.steplayout.StepLayouter;
import org.openecard.ws.gui.v1.OutputInfoUnitType;
import org.openecard.ws.gui.v1.Step;


/**
 * The StepFrame class represents a single step. The actual layouting is however
 * deferred to a layouting component.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class StepFrame {

    private final JPanel rootPanel;
    private final JButton backButton;
    private final JButton forwardButton;
    private final JButton cancelButton;

    private final List<StepComponent> components;

    public StepFrame(Step step, String dialogType, int idx) {
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

        // fill content panel - this is done with an external class which knows all about the actual layout
        StepLayouter stepLayouter = StepLayouter.create(step.getInfoUnit(), dialogType, step.getName());
        Container contentPanel = stepLayouter.getPanel();
        rootPanel.add(contentPanel, BorderLayout.CENTER);
        components = stepLayouter.getComponents();
    }

    public Container getPanel() {
        return rootPanel;
    }

    public JButton getBackButton() {
        return this.backButton;
    }
    public JButton getForwardButton() {
        return this.forwardButton;
    }
    public JButton getCancelButton() {
        return this.cancelButton;
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
    public List<OutputInfoUnitType> getResultContent() {
        ArrayList<OutputInfoUnitType> result = new ArrayList<OutputInfoUnitType>(components.size());
        for (StepComponent next : components) {
            if (next.isValueType()) {
                result.add(next.getValue());
            }
        }
        return result;
    }

}
