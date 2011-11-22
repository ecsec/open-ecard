package org.openecard.client.gui.swing.steplayout;

import java.awt.Container;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import org.openecard.client.gui.swing.components.Checkbox;
import org.openecard.client.gui.swing.components.Hyperlink;
import org.openecard.client.gui.swing.components.Passwordinput;
import org.openecard.client.gui.swing.components.Radiobutton;
import org.openecard.client.gui.swing.components.StepComponent;
import org.openecard.client.gui.swing.components.Text;
import org.openecard.client.gui.swing.components.Textinput;
import org.openecard.ws.gui.v1.InfoUnitType;


/**
 * Default layouter. Ugly but works in any case.
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class DefaultStepLayouter extends StepLayouter {

    private final ArrayList<StepComponent> components;
    private final JPanel contentPanel;

    protected DefaultStepLayouter(List<InfoUnitType> infoUnits) {
        components = new ArrayList<StepComponent>(infoUnits.size());
        GridLayout contentLayout = new GridLayout(0, 1);
        contentPanel = new JPanel(contentLayout);

        // create content
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
            components.add(nextComponent);
            contentPanel.add(nextComponent.getComponent());
        }
    }


    @Override
    public List<StepComponent> getComponents() {
        return components;
    }

    @Override
    public Container getPanel() {
        return contentPanel;
    }

}
