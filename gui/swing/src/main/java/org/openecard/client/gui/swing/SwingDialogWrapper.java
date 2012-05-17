/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openecard.client.gui.swing;

import java.awt.Color;
import java.awt.Container;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIDefaults;
import javax.swing.UIManager;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class SwingDialogWrapper implements DialogWrapper {

    private JFrame dialog;

    private void initializeLookAndFeel() {
	// Load UI defaults
	UIDefaults defaults = UIManager.getDefaults();
	defaults.put("Panel.background", Color.WHITE);

	try {
	    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	} catch (Exception ignore) {
	}
    }

    private void initializeIcon() {
	ImageIcon logo = new ImageIcon();
	URL url = Logo.class.getResource("/openecardwhite.gif");
	if (url != null) {
	    Toolkit toolkit = Toolkit.getDefaultToolkit();
	    Image image = toolkit.getImage(url);
	    image = image.getScaledInstance(32, 32, Image.SCALE_SMOOTH);
	    logo.setImage(image);
	}
	if (dialog != null) {
	    dialog.setIconImage(logo.getImage());
	}
    }

    public SwingDialogWrapper() {
	initializeLookAndFeel();

	dialog = new JFrame();
	dialog.setSize(600, 400);

	initializeIcon();

	dialog.setVisible(false);
	dialog.setDefaultCloseOperation(JDialog.EXIT_ON_CLOSE);
    }

    @Override
    public void setTitle(String title) {
	dialog.setTitle(title);
    }

    @Override
    public Container getRootPanel() {
	return dialog.getContentPane();
    }

    @Override
    public void showDialog() {
	this.dialog.setVisible(true);
    }

    @Override
    public void hideDialog() {
	this.dialog.setVisible(false);
    }
}
