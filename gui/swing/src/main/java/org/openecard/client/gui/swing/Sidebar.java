package org.openecard.client.gui.swing;

//import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
//import javax.swing.JPanel;


/**
 * A sidebar represents the progress of the user consent. Every step has an
 * entry and the current step is selected.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @editor Florian Feldmann <florian.feldmann@rub.de>
 */
public class Sidebar {

    private final static ImageIcon logo;
    static {
        // load logo
        logo = new ImageIcon();
        URL url = Sidebar.class.getResource("/openecardwhite.gif");
        if (url != null) {
            // seems like someone was so kind to bundle an image
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Image image = toolkit.getImage(url);
            // TODO: size is scaled, when this is known, the size of the image should be correct to save space
            image = image.getScaledInstance(90, 90, Image.SCALE_SMOOTH);
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
        
        // color info for sidebar list element
        steps.setSelectionForeground(Color.BLACK); // selected entry black
        Color backgroundColor = sidebarPanel.getBackground(); // set background color of list element
        steps.setSelectionBackground(backgroundColor);
        steps.setBackground(backgroundColor);
        steps.setForeground(Color.GRAY); // unselected entries grayed out
        
        // organize layout
        GridBagLayout layout = new GridBagLayout();
        sidebarPanel.setLayout(layout);
        
        // set layout constraints for list element
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.weighty = 1.0; // set weights to 100% - maximum available free
        c.weightx = 1.0; // space above and below = centered vertically
        c.anchor = GridBagConstraints.CENTER;
        c.gridx = 0;
        c.gridy = GridBagConstraints.RELATIVE;
        sidebarPanel.add(steps, c);
        
        // add logo
        JLabel logoLabel = new JLabel(logo);
        c.anchor = GridBagConstraints.PAGE_END; // set to bottom
        c.fill = GridBagConstraints.HORIZONTAL; // set centered
        c.gridx = 0;
        c.gridy = GridBagConstraints.RELATIVE;
        c.weightx = 0; // set weights to zero - no free space around logo
        c.weighty = 0;
        sidebarPanel.add(logoLabel, c);

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
