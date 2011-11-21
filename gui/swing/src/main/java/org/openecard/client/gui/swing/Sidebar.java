package org.openecard.client.gui.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import javax.swing.JList;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class Sidebar {

    private final Container panel;
    private final JList steps;
    private final int numSteps;

    public Sidebar(Container panel, String... stepNames) {
        this.panel = panel;
        this.numSteps = stepNames.length;
        this.steps = new JList(stepNames);
        this.steps.setSelectedIndex(0);
        BorderLayout layout = new BorderLayout();
        this.panel.setLayout(layout);
        this.panel.add(steps, BorderLayout.CENTER);

        // configure steplist
        this.steps.setEnabled(false);
    }


    public void next() {
        selectIdx(steps.getSelectedIndex()+1);
    }

    public void previous() {
        selectIdx(steps.getSelectedIndex()-1);
    }

    public void selectIdx(int idx) {
        if (idx >= 0 && idx < numSteps) {
            steps.setSelectedIndex(idx);
        }
    }

}
