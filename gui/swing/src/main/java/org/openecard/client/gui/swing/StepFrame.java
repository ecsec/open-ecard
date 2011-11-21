package org.openecard.client.gui.swing;

import org.openecard.client.gui.swing.components.StepComponent;
import org.openecard.client.gui.swing.components.Radiobutton;
import org.openecard.client.gui.swing.components.Passwordinput;
import org.openecard.client.gui.swing.components.Hyperlink;
import org.openecard.client.gui.swing.components.Checkbox;
import org.openecard.client.gui.swing.components.Text;
import org.openecard.client.gui.swing.components.Textinput;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.openecard.ws.gui.v1.InfoUnitType;
import org.openecard.ws.gui.v1.Step;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class StepFrame {

    private final JPanel rootPanel;
    private final JButton backButton;
    private final JButton forwardButton;
    private final JButton cancelButton;

    private final ArrayList<StepComponent> components;

    public StepFrame(Step step) {
        BorderLayout layout = new BorderLayout();
        this.rootPanel = new JPanel();
        this.rootPanel.setLayout(layout);
        FlowLayout buttonLayout = new FlowLayout();
        JPanel buttonPanel = new JPanel(buttonLayout);
        this.rootPanel.add(buttonPanel, BorderLayout.SOUTH);
        // create button elements
        backButton = new JButton();
        forwardButton = new JButton();
        cancelButton = new JButton();
        buttonPanel.add(backButton);
        buttonPanel.add(forwardButton);
        buttonPanel.add(cancelButton);

        // create content panel
        JPanel contentPanel = new JPanel();
        GridLayout contentLayout = new GridLayout(0, 1);
        contentPanel.setLayout(contentLayout);
        this.rootPanel.add(contentPanel, BorderLayout.CENTER);
        // create content
        List<InfoUnitType> infoUnits = step.getInfoUnit();
        this.components = new ArrayList<StepComponent>(infoUnits.size());
        for (InfoUnitType next : infoUnits) {
            StepComponent nextComponent = null;
            if (next.getCheckBox() != null) {
                nextComponent = new Checkbox(next.getCheckBox());
            } else if (next.getHyperLink() != null) {
                nextComponent = new Hyperlink(next.getHyperLink());
            } else if (next.getPasswordInput() != null) {
                nextComponent = new Passwordinput(next.getPasswordInput());
            } else if (next.getRadio() != null) {
                nextComponent = new Radiobutton(next.getRadio());
            } else if (next.getText() != null) {
                nextComponent = new Text(next.getText());
            } else if (next.getTextInput() != null) {
                nextComponent = new Textinput(next.getTextInput());
            }
            // add to list panel
            this.components.add(nextComponent);
            contentPanel.add(nextComponent.getComponent());
        }
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

    public boolean validate() {
        for (StepComponent next : this.components) {
            if (next.isValueType() && ! next.validate()) {
                return false;
            }
        }
        return true;
    }

    public List<InfoUnitType> getResultContent() {
        ArrayList<InfoUnitType> result = new ArrayList<InfoUnitType>(this.components.size());
        for (StepComponent next : this.components) {
            if (next.isValueType()) {
                result.add(next.getValue());
            }
        }
        return result;
    }

}
