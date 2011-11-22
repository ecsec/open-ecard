package org.openecard.client.gui.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Image;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;


/**
 * A sidebar represents the progress of the user consent. Every step has an
 * entry and the current step is selected.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class Sidebar {

    private final Container sidebarPanel;
    private final JList steps;
    private final int numSteps;

    /**
     * Create a Sidebar instance and draw its content on the supplied panel.
     *
     * @param sidebarPanel Panel to draw sidebar content on.
     * @param stepNames Names of the steps as they are displayed in the sidebar.
     */
    public Sidebar(Container sidebarPanel, String... stepNames) {
        // create step components
        this.sidebarPanel = sidebarPanel;
        this.numSteps = stepNames.length;
        this.steps = new JList();
        this.steps.removeAll();
        this.steps.validate();
        this.steps.setListData(stepNames);
        this.steps.setSelectedIndex(0);
        BorderLayout layout = new BorderLayout();
        this.sidebarPanel.setLayout(layout);
        this.sidebarPanel.add(steps, BorderLayout.CENTER);

        // add logo
        ImageIcon logo = new ImageIcon(Sidebar.class.getResource("/openecard.png"));
        Image scaled = logo.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
        logo.setImage(scaled);
        JLabel logoLabel = new JLabel(logo);
        this.sidebarPanel.add(logoLabel, BorderLayout.SOUTH);

        // configure steplist
        // remove listeners so no selection is possible through the ui
        for (MouseListener m : this.steps.getMouseListeners()) {
            this.steps.removeMouseListener(m);
        }
        for (MouseMotionListener m : this.steps.getMouseMotionListeners()) {
            this.steps.removeMouseMotionListener(m);
        }
        this.steps.setFocusable(false);
    }


    /**
     * Mark the step belonging to the supplied index as selected. The sidebar
     * emphasises this step accordingly.<br/>
     * Values outside the defined range (0 .. numsteps-1) are ignored.
     *
     * @param idx Index starting from 0.
     */
    public void selectIdx(int idx) {
        if (idx >= 0 && idx < numSteps) {
            steps.setSelectedIndex(idx);
        }
    }

}
