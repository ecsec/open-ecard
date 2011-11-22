package org.openecard.client.gui.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.net.URL;
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

    private final static ImageIcon logo;
    static {
        // load logo
        logo = new ImageIcon();
        URL url = Sidebar.class.getResource("/openecard.gif");
        if (url != null) {
            // seems like someone was so kind to bundle an image
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Image image = toolkit.getImage(url);
            // TODO: size is scaled, when this is known, the size of the image should be correct to save space
            image = image.getScaledInstance(150, 150, Image.SCALE_SMOOTH);
            logo.setImage(image);
        }
    }

    private final JList steps;
    private final int numSteps;

    /**
     * Create a Sidebar instance and draw its content on the supplied panel.
     *
     * @param sidebarPanel Panel to draw sidebar content on.
     * @param stepNames Names of the steps as they are displayed in the sidebar.
     */
    public Sidebar(Container sidebarPanel, String... stepNames) {
        this.numSteps = stepNames.length;

        // create step components
        steps = new JList();
        steps.removeAll();
        steps.validate();
        steps.setListData(stepNames);
        steps.setSelectedIndex(0);
        BorderLayout layout = new BorderLayout();
        sidebarPanel.setLayout(layout);
        sidebarPanel.add(steps, BorderLayout.CENTER);
        // add logo
        JLabel logoLabel = new JLabel(logo);
        sidebarPanel.add(logoLabel, BorderLayout.SOUTH);

        // configure steplist (behaviour and style)
        // remove listeners so no selection is possible through the ui
        for (MouseListener m : steps.getMouseListeners()) {
            steps.removeMouseListener(m);
        }
        for (MouseMotionListener m : steps.getMouseMotionListeners()) {
            this.steps.removeMouseMotionListener(m);
        }
        steps.setFocusable(false);
        // TODO: configure style

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
