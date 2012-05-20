/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openecard.client.gui.swing;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JDialog;
import javax.swing.JFrame;
import org.openecard.client.gui.swing.common.GUIDefaults;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class SwingDialogWrapper implements DialogWrapper {

    private JFrame dialog;

    public SwingDialogWrapper() {
	// Initialize Look and Feel
	GUIDefaults.initialize();

	dialog = new JFrame();
	dialog.setSize(600, 400);

	// Center window
	Toolkit toolkit = Toolkit.getDefaultToolkit();
	Dimension screenSize = toolkit.getScreenSize();
	int x = (screenSize.width - dialog.getWidth()) / 2;
	int y = (screenSize.height - dialog.getHeight()) / 2;
	dialog.setLocation(x, y);

	AppTray tray = new AppTray();
	tray.initialize();

	dialog.setIconImage(GUIDefaults.getImage("Frame.icon", 45, 45).getImage());

	dialog.setVisible(false);
	//FIXME
	dialog.setDefaultCloseOperation(JDialog.EXIT_ON_CLOSE);
    }

    @Override
    public void setTitle(String title) {
	dialog.setTitle(title);
    }

    @Override
    public Container getContentPane() {
	return dialog.getContentPane();
    }

    @Override
    public void show() {
	this.dialog.setVisible(true);
    }

    @Override
    public void hide() {
	this.dialog.setVisible(false);
    }
}
